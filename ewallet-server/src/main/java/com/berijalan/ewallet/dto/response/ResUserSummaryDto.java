package com.berijalan.ewallet.dto.response;

import java.time.LocalDateTime;

public record ResUserSummaryDto(
        Long userId,
        String name,
        String email,
        LocalDateTime createdAt,
        String role
) {}
