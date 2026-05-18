package com.berijalan.ewallet.exception;

/**
 * Dipakai untuk pelanggaran aturan request atau bisnis yang dipetakan menjadi HTTP 400.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
