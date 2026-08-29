package com.dpc.smart_staffing_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ConsultantMatchDTO(
        ConsultantResponseDTO consultant,
        @JsonProperty("matchScore") double matchScore,
        @JsonProperty("matchingScore") double matchingScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        String matchReason
) {
    public ConsultantMatchDTO(ConsultantResponseDTO consultant, double matchScore, List<String> matchedSkills, List<String> missingSkills, String matchReason) {
        this(consultant, matchScore, matchScore, matchedSkills, missingSkills, matchReason);
    }
}
