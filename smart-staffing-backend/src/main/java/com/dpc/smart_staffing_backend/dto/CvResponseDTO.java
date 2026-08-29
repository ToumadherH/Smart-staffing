package com.dpc.smart_staffing_backend.dto;

import java.time.Instant;

public record CvResponseDTO(
        Long id,
        String fileName,
        String contentType,
        Instant uploadedAt,
        String downloadUrl,
        String extractedText,
        String extractedEmail,
        String extractedPhone,
        String extractedSkillsText
) {
    public CvResponseDTO(Long id, String fileName, String contentType, Instant uploadedAt, String downloadUrl) {
        this(id, fileName, contentType, uploadedAt, downloadUrl, null, null, null, null);
    }
}
