package com.berijalan.ewallet.dto.response;

public record ResPaymentDto(
        Long transactionId,
        String referenceId,
        Long amount,
        Long newBalance,
        String merchantName,
        String type,
        String status
) {}
