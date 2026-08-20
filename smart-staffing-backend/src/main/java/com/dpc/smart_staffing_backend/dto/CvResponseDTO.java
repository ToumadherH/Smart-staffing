package com.dpc.smart_staffing_backend.dto;

import java.time.Instant;

public record CvResponseDTO(
        Long id,
        String fileName,
        String contentType,
        Instant uploadedAt,
        String downloadUrl
) {
}
