package com.berijalan.ewallet.dto.response;

import java.time.LocalDateTime;

public record ResTransactionItemDto(
        Long transactionId,
        Long userId,
        String userName,
        String userEmail,
        String referenceId,
        Long amount,
        Long baseAmount,
        Long taxAmount,
        Long balanceBefore,
        Long balanceAfter,
        String type,
        String status,
        String description,
        String merchantName,
        LocalDateTime createdAt
) {}
