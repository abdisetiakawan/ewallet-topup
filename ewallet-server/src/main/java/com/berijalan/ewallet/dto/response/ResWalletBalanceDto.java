package com.berijalan.ewallet.dto.response;

import java.time.LocalDateTime;

public record ResWalletBalanceDto(
        Long balance,
        LocalDateTime updatedAt
) {}
