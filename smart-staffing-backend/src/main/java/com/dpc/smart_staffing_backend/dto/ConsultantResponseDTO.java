package com.dpc.smart_staffing_backend.dto;

import com.dpc.smart_staffing_backend.entity.Availability;

import java.util.List;

// Shape returned by the API. Deliberately separate from ConsultantRequestDTO: the response
// includes 'id' and fully-resolved SkillDTOs (with id + category), which a request never has.
public record ConsultantResponseDTO(
        Long id,
        String name,
        String email,
        String phone,
        Integer yearsOfExperience,
        Availability availability,
        String currentMission,
        String location,
        List<String> languages,
        List<SkillDTO> skills,
        CvResponseDTO cv
) {
}
