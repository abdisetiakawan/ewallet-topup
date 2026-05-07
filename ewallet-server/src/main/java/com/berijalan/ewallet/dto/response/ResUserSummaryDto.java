package com.berijalan.ewallet.dto.response;

public record ResUserSummaryDto(
        Long userId,
        String name,
        String email
) {}
