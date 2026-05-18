package com.berijalan.ewallet.exception;

/**
 * Dipakai saat credential atau token tidak valid dan dipetakan menjadi HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
