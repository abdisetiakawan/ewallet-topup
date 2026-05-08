package com.berijalan.ewallet.dto.response;

public record ResTopupDto(
        Long transactionId,
        String referenceId,
        Long newBalance,
        String type,
        String status
) {}
