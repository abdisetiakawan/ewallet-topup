package com.berijalan.ewallet.service;

import java.time.LocalDateTime;

public record WalletBalanceCache(
        Long balance,
        LocalDateTime updatedAt
) {}
