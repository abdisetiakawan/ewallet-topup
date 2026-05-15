package com.berijalan.ewallet.dto.response;

public record ResRefreshTokenDto(
        String token,
        String tokenType,
        long expiresIn
) {
}
