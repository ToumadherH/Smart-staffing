package com.dpc.smart_staffing_backend.dto;

import com.dpc.smart_staffing_backend.entity.StaffingRequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record StaffingRequestRequestDTO(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Client name is required")
        String clientName,

        String location,
        Integer yearsOfExperienceRequired,
        String description,

        StaffingRequestStatus status,

        List<SkillDTO> requiredSkills
) {}
