package com.berijalan.ewallet.dto.response;

import java.time.LocalDateTime;

public record ResTopupDto(
        Long transactionId,
        String referenceId,
        Long amount,
        Long newBalance,
        String type,
        String status,
        LocalDateTime createdAt
) {}
