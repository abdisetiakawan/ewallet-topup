package com.berijalan.ewallet.exception;

/**
 * Dipakai saat request valid bertabrakan dengan state terkini, misalnya idempotency atau email unik.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
