package com.dpc.smart_staffing_backend.exception;

import java.time.Instant;
import java.util.Map;

// One consistent JSON shape for every error the API returns.
// fieldErrors is null except for validation failures (400s from @Valid).
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
