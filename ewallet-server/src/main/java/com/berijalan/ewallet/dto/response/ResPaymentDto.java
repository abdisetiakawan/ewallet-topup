package com.berijalan.ewallet.dto.response;

public record ResPaymentDto(
        Long transactionId,
        String referenceId,
        Long amount,
        Long balanceBefore,
        Long balanceAfter,
        String description,
        String merchantName,
        String type,
        String status
) {}
