package com.berijalan.ewallet.dto.response;

public record ResEmailChangeDto(
        String newEmail,
        int expiresInMinutes
) {}
