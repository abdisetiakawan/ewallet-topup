package com.berijalan.ewallet.dto.response;

import java.time.LocalDateTime;

public record ResTransactionItemDto(
        Long transactionId,
        String referenceId,
        Long amount,
        String type,
        String status,
        String merchantName,
        LocalDateTime createdAt
) {}