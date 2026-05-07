package com.berijalan.ewallet.dto.response;

public record ResTopupDto(
        Long transactionId,
        Long newBalance,
        String type,
        String status
) {}
