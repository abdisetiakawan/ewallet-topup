package com.berijalan.ewallet.dto.response;

import java.time.LocalDateTime;

public record ResTopupDto(
        Long transactionId,
        Long amount,
        Long balanceBefore,
        Long balanceAfter,
        String type,
        String status,
        LocalDateTime createdAt
) {}
