package com.berijalan.ewallet.dto.response;

public record ResTopupDto(
        Long transactionId,
        Long amount,
        Long balanceBefore,
        Long balanceAfter,
        String type,
        String status
) {}
