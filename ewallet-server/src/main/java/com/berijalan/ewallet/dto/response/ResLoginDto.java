package com.berijalan.ewallet.dto.response;

public record ResLoginDto(
        String token,
        String tokenType,
        long expiresIn,
        ResUserSummaryDto user
) {}
