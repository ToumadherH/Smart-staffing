package com.dpc.smart_staffing_backend.mapper;

import com.dpc.smart_staffing_backend.dto.SkillDTO;
import com.dpc.smart_staffing_backend.dto.StaffingRequestDTO;
import com.dpc.smart_staffing_backend.entity.StaffingRequest;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class StaffingRequestMapper {

    public StaffingRequestDTO toDTO(StaffingRequest entity) {
        if (entity == null) return null;

        List<SkillDTO> skills = entity.getRequiredSkills().stream()
                .map(s -> new SkillDTO(s.getId(), s.getName(), s.getCategory()))
                .sorted(Comparator.comparing(SkillDTO::name))
                .toList();

        return new StaffingRequestDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getClientName(),
                entity.getLocation(),
                entity.getYearsOfExperienceRequired(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt(),
                skills
        );
    }
}
