package com.dpc.smart_staffing_backend.service;

import com.dpc.smart_staffing_backend.dto.SkillDTO;
import com.dpc.smart_staffing_backend.dto.StaffingRequestDTO;
import com.dpc.smart_staffing_backend.dto.StaffingRequestRequestDTO;
import com.dpc.smart_staffing_backend.entity.Skill;
import com.dpc.smart_staffing_backend.entity.StaffingRequest;
import com.dpc.smart_staffing_backend.entity.StaffingRequestStatus;
import com.dpc.smart_staffing_backend.exception.ResourceNotFoundException;
import com.dpc.smart_staffing_backend.mapper.StaffingRequestMapper;
import com.dpc.smart_staffing_backend.repository.SkillRepository;
import com.dpc.smart_staffing_backend.repository.StaffingRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class StaffingRequestService {

    private final StaffingRequestRepository staffingRequestRepository;
    private final SkillRepository skillRepository;
    private final StaffingRequestMapper staffingRequestMapper;

    public StaffingRequestService(StaffingRequestRepository staffingRequestRepository,
                                  SkillRepository skillRepository,
                                  StaffingRequestMapper staffingRequestMapper) {
        this.staffingRequestRepository = staffingRequestRepository;
        this.skillRepository = skillRepository;
        this.staffingRequestMapper = staffingRequestMapper;
    }

    public List<StaffingRequestDTO> listAll() {
        return staffingRequestRepository.findAll().stream()
                .map(staffingRequestMapper::toDTO)
                .toList();
    }

    public StaffingRequestDTO getById(Long id) {
        StaffingRequest entity = staffingRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staffing request not found with id " + id));
        return staffingRequestMapper.toDTO(entity);
    }

    @Transactional
    public StaffingRequestDTO create(StaffingRequestRequestDTO requestDTO) {
        StaffingRequest entity = new StaffingRequest(
                requestDTO.title(),
                requestDTO.clientName(),
                requestDTO.location(),
                requestDTO.yearsOfExperienceRequired(),
                requestDTO.description(),
                requestDTO.status()
        );

        entity.setRequiredSkills(resolveSkills(requestDTO.requiredSkills()));

        StaffingRequest saved = staffingRequestRepository.save(entity);
        return staffingRequestMapper.toDTO(saved);
    }

    @Transactional
    public StaffingRequestDTO update(Long id, StaffingRequestRequestDTO requestDTO) {
        StaffingRequest entity = staffingRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staffing request not found with id " + id));

        entity.setTitle(requestDTO.title());
        entity.setClientName(requestDTO.clientName());
        entity.setLocation(requestDTO.location());
        entity.setYearsOfExperienceRequired(requestDTO.yearsOfExperienceRequired());
        entity.setDescription(requestDTO.description());
        if (requestDTO.status() != null) {
            entity.setStatus(requestDTO.status());
        }
        entity.setRequiredSkills(resolveSkills(requestDTO.requiredSkills()));

        StaffingRequest updated = staffingRequestRepository.save(entity);
        return staffingRequestMapper.toDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!staffingRequestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Staffing request not found with id " + id);
        }
        staffingRequestRepository.deleteById(id);
    }

    private Set<Skill> resolveSkills(List<SkillDTO> skillDTOs) {
        if (skillDTOs == null || skillDTOs.isEmpty()) {
            return new HashSet<>();
        }
        Set<Skill> skills = new HashSet<>();
        for (SkillDTO dto : skillDTOs) {
            if (dto.name() == null || dto.name().isBlank()) continue;
            Skill skill = skillRepository.findByNameIgnoreCase(dto.name().trim())
                    .orElseGet(() -> skillRepository.save(new Skill(dto.name().trim(), dto.category() != null ? dto.category() : "General")));
            skills.add(skill);
        }
        return skills;
    }
}
