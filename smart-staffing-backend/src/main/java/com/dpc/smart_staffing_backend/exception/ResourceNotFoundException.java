package com.dpc.smart_staffing_backend.exception;

// Thrown when a lookup by id finds nothing. Mapped to HTTP 404 by the
// global exception handler (Step G).
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
