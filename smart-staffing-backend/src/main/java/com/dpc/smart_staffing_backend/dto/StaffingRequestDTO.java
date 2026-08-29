package com.dpc.smart_staffing_backend.dto;

import com.dpc.smart_staffing_backend.entity.StaffingRequestStatus;

import java.time.Instant;
import java.util.List;

public record StaffingRequestDTO(
        Long id,
        String title,
        String clientName,
        String location,
        Integer yearsOfExperienceRequired,
        String description,
        StaffingRequestStatus status,
        Instant createdAt,
        List<SkillDTO> requiredSkills
) {}
