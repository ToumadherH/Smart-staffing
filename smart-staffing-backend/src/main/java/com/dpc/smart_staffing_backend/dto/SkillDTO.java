package com.dpc.smart_staffing_backend.dto;

import jakarta.validation.constraints.NotBlank;

// 'id' is ignored on input (the service resolves skills by name) and populated on output.
public record SkillDTO(

        Long id,

        @NotBlank(message = "Skill name is required")
        String name,

        @NotBlank(message = "Skill category is required")
        String category
) {
}
