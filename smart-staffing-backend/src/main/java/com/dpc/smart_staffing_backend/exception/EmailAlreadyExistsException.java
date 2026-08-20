package com.dpc.smart_staffing_backend.exception;

// Thrown when creating/updating a consultant with an email already used by another
// consultant. Mapped to HTTP 409 (Conflict) by the global exception handler (Step G).
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
