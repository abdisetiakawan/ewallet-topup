package com.berijalan.ewallet.dto.response;

public record ResTopupDto(
        Long transactionId,
        Long amount,
        Long balanceBefore,
        Long balanceAfter,
        Long newBalance,
        String type,
        String status
) {}
