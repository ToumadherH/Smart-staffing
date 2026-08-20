package com.dpc.smart_staffing_backend.service;

import com.dpc.smart_staffing_backend.dto.ConsultantRequestDTO;
import com.dpc.smart_staffing_backend.dto.ConsultantResponseDTO;
import com.dpc.smart_staffing_backend.dto.CvResponseDTO;
import com.dpc.smart_staffing_backend.dto.SkillDTO;
import com.dpc.smart_staffing_backend.entity.Consultant;
import com.dpc.smart_staffing_backend.entity.Cv;
import com.dpc.smart_staffing_backend.entity.Skill;
import com.dpc.smart_staffing_backend.exception.EmailAlreadyExistsException;
import com.dpc.smart_staffing_backend.exception.ResourceNotFoundException;
import com.dpc.smart_staffing_backend.mapper.ConsultantMapper;
import com.dpc.smart_staffing_backend.repository.ConsultantRepository;
import com.dpc.smart_staffing_backend.repository.CvRepository;
import com.dpc.smart_staffing_backend.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Business logic for consultants lives here: controllers stay thin, repositories stay
// focused on persistence. Read-only by default; individual write methods override that.
@Service
@Transactional(readOnly = true)
public class ConsultantService {

    private final ConsultantRepository consultantRepository;
    private final SkillRepository skillRepository;
    private final CvRepository cvRepository;
    private final CvStorageService cvStorageService;
    private final ConsultantMapper mapper;

    public ConsultantService(ConsultantRepository consultantRepository,
                              SkillRepository skillRepository,
                              CvRepository cvRepository,
                              CvStorageService cvStorageService,
                              ConsultantMapper mapper) {
        this.consultantRepository = consultantRepository;
        this.skillRepository = skillRepository;
        this.cvRepository = cvRepository;
        this.cvStorageService = cvStorageService;
        this.mapper = mapper;
    }

    public List<ConsultantResponseDTO> getAllConsultants() {
        return consultantRepository.findAll().stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    public ConsultantResponseDTO getConsultantById(Long id) {
        return mapper.toResponseDTO(findConsultantOrThrow(id));
    }

    @Transactional
    public ConsultantResponseDTO createConsultant(ConsultantRequestDTO dto) {
        if (consultantRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("A consultant with email " + dto.email() + " already exists");
        }

        Consultant consultant = mapper.toEntity(dto);
        consultant.setSkills(resolveSkills(dto.skills()));

        Consultant saved = consultantRepository.save(consultant);
        return mapper.toResponseDTO(saved);
    }

    @Transactional
    public ConsultantResponseDTO updateConsultant(Long id, ConsultantRequestDTO dto) {
        Consultant consultant = findConsultantOrThrow(id);

        boolean emailChanged = !consultant.getEmail().equalsIgnoreCase(dto.email());
        if (emailChanged && consultantRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("A consultant with email " + dto.email() + " already exists");
        }

        mapper.updateEntity(consultant, dto);
        consultant.setSkills(resolveSkills(dto.skills()));

        // No explicit save(): 'consultant' is a managed entity inside this transaction,
        // so Hibernate flushes the changes automatically at commit (dirty checking).
        return mapper.toResponseDTO(consultant);
    }

    @Transactional
    public void deleteConsultant(Long id) {
        Consultant consultant = findConsultantOrThrow(id);
        if (consultant.getCv() != null) {
            cvStorageService.delete(consultant.getCv().getStoredFileName());
        }
        consultantRepository.delete(consultant);
    }

    @Transactional
    public CvResponseDTO uploadCv(Long consultantId, MultipartFile file) {
        Consultant consultant = findConsultantOrThrow(consultantId);
        String storedFileName = cvStorageService.store(file);
        Cv previousCv = consultant.getCv();
        Cv cv = new Cv(file.getOriginalFilename(), storedFileName, file.getContentType(), java.time.Instant.now(), consultant);
        consultant.setCv(cv);
        Cv saved = cvRepository.save(cv);

        if (previousCv != null) {
            cvStorageService.delete(previousCv.getStoredFileName());
        }
        return new CvResponseDTO(saved.getId(), saved.getFileName(), saved.getContentType(), saved.getUploadedAt(),
                "/api/consultants/" + consultantId + "/cv/download");
    }

    public CvFile downloadCv(Long consultantId) {
        Cv cv = cvRepository.findByConsultantId(consultantId)
                .orElseThrow(() -> new ResourceNotFoundException("CV not found for consultant with id " + consultantId));
        return new CvFile(cv.getFileName(), cv.getContentType(), cvStorageService.load(cv.getStoredFileName()));
    }

    public record CvFile(String fileName, String contentType, Resource resource) { }

    private Consultant findConsultantOrThrow(Long id) {
        return consultantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found with id " + id));
    }

    // Reuses an existing skill by name (case-insensitive) or creates it on the fly.
    // New skills must be saved explicitly here: the Consultant-Skill relationship has
    // no cascade, so an unsaved (transient) Skill would fail when the consultant saves.
    private Set<Skill> resolveSkills(List<SkillDTO> skillDTOs) {
        if (skillDTOs == null || skillDTOs.isEmpty()) {
            return new HashSet<>();
        }

        Set<Skill> skills = new HashSet<>();
        for (SkillDTO skillDTO : skillDTOs) {
            Skill skill = skillRepository.findByNameIgnoreCase(skillDTO.name())
                    .orElseGet(() -> skillRepository.save(new Skill(skillDTO.name(), skillDTO.category())));
            skills.add(skill);
        }
        return skills;
    }
}
