package com.berijalan.ewallet.exception;


/**
 * Dipakai saat resource domain tidak ditemukan dan dipetakan menjadi HTTP 404 oleh {@link GlobalExceptionHandler}.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
