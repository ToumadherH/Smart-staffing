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
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ConsultantService {

    private final ConsultantRepository consultantRepository;
    private final SkillRepository skillRepository;
    private final CvRepository cvRepository;
    private final CvStorageService cvStorageService;
    private final CvExtractionService cvExtractionService;
    private final ConsultantMapper mapper;

    public ConsultantService(ConsultantRepository consultantRepository,
                              SkillRepository skillRepository,
                              CvRepository cvRepository,
                              CvStorageService cvStorageService,
                              CvExtractionService cvExtractionService,
                              ConsultantMapper mapper) {
        this.consultantRepository = consultantRepository;
        this.skillRepository = skillRepository;
        this.cvRepository = cvRepository;
        this.cvStorageService = cvStorageService;
        this.cvExtractionService = cvExtractionService;
        this.mapper = mapper;
    }

    public List<ConsultantResponseDTO> listAllConsultants() {
        return consultantRepository.findAll().stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    public List<ConsultantResponseDTO> getAllConsultants() {
        return listAllConsultants();
    }

    public ConsultantResponseDTO getConsultantById(Long id) {
        return mapper.toResponseDTO(findConsultantOrThrow(id));
    }

    @Transactional
    public ConsultantResponseDTO createConsultant(ConsultantRequestDTO dto) {
        if (consultantRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("A consultant with email '" + dto.email() + "' already exists.");
        }

        Consultant consultant = mapper.toEntity(dto);
        consultant.setSkills(resolveSkills(dto.skills()));

        Consultant saved = consultantRepository.save(consultant);
        return mapper.toResponseDTO(saved);
    }

    @Transactional
    public ConsultantResponseDTO updateConsultant(Long id, ConsultantRequestDTO dto) {
        Consultant consultant = findConsultantOrThrow(id);

        if (!consultant.getEmail().equalsIgnoreCase(dto.email()) && consultantRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("A consultant with email '" + dto.email() + "' already exists.");
        }

        mapper.updateEntity(consultant, dto);
        consultant.setSkills(resolveSkills(dto.skills()));

        Consultant saved = consultantRepository.save(consultant);
        return mapper.toResponseDTO(saved);
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

        // Perform CV extraction
        try (InputStream is = cvStorageService.load(storedFileName).getInputStream()) {
            CvExtractionService.ExtractionResult result = cvExtractionService.extract(is, file.getOriginalFilename());
            cv.setExtractedText(result.extractedText());
            cv.setExtractedEmail(result.extractedEmail());
            cv.setExtractedPhone(result.extractedPhone());

            if (!result.extractedSkills().isEmpty()) {
                String skillsListStr = String.join(", ", result.extractedSkills());
                cv.setExtractedSkillsText(skillsListStr);

                // Auto-associate extracted skills with consultant without creating duplicate Skill records
                Set<Skill> currentSkills = consultant.getSkills();
                for (String skillName : result.extractedSkills()) {
                    Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                            .orElseGet(() -> skillRepository.save(new Skill(skillName, "Technical")));
                    currentSkills.add(skill);
                }
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ConsultantService.class).warn("Failed to extract CV content for consultant {}: {}", consultantId, e.getMessage(), e);
        }

        if (previousCv != null) {
            consultant.setCv(null);
            cvRepository.delete(previousCv);
            cvRepository.flush();
            cvStorageService.delete(previousCv.getStoredFileName());
        }

        consultant.setCv(cv);
        Cv saved = cvRepository.save(cv);
        consultantRepository.save(consultant);

        return mapper.toResponseDTO(consultant).cv();
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

    private Set<Skill> resolveSkills(List<SkillDTO> skillDTOs) {
        if (skillDTOs == null || skillDTOs.isEmpty()) {
            return new HashSet<>();
        }

        Set<Skill> skills = new HashSet<>();
        for (SkillDTO skillDTO : skillDTOs) {
            if (skillDTO.name() == null || skillDTO.name().isBlank()) continue;
            Skill skill = skillRepository.findByNameIgnoreCase(skillDTO.name().trim())
                    .orElseGet(() -> skillRepository.save(new Skill(skillDTO.name().trim(), skillDTO.category() != null ? skillDTO.category() : "General")));
            skills.add(skill);
        }
        return skills;
    }
}
