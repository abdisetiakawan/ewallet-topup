package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response refresh token berisi access token baru.")
public record ResRefreshTokenDto(
        @Schema(description = "Access token JWT baru.", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,
        @Schema(description = "Tipe token untuk header Authorization.", example = "Bearer")
        String tokenType,
        @Schema(description = "Masa berlaku access token dalam detik.", example = "900")
        long expiresIn
) {
}
