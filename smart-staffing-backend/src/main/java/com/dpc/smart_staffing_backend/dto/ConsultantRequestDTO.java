package com.dpc.smart_staffing_backend.dto;

import com.dpc.smart_staffing_backend.entity.Availability;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Shape accepted by POST/PUT /api/consultants. No 'id' field: the id is assigned by the
// database, or taken from the URL path on updates (PUT /api/consultants/{id}).
public record ConsultantRequestDTO(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        String email,

        String phone,

        @NotNull(message = "Years of experience is required")
        @Min(value = 0, message = "Years of experience cannot be negative")
        Integer yearsOfExperience,

        @NotNull(message = "Availability is required")
        Availability availability,

        String currentMission,

        String location,

        List<String> languages,

        // @Valid on the type argument cascades validation into each SkillDTO
        // (e.g. rejects a blank skill name) — the modern spot for it, not on the list itself.
        List<@Valid SkillDTO> skills
) {
}
