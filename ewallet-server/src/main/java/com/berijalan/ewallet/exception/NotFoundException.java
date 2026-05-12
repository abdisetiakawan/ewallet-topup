package com.berijalan.ewallet.exception;


/**
 * Thrown when a requested resource cannot be found.
 * Maps to HTTP 404 Not Found via {@link GlobalExceptionHandler}.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
