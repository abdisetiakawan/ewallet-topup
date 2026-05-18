package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response login berisi access token dan ringkasan pengguna.")
public record ResLoginDto(
        @Schema(description = "Access token JWT untuk endpoint protected.", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,
        @Schema(description = "Tipe token untuk header Authorization.", example = "Bearer")
        String tokenType,
        @Schema(description = "Masa berlaku access token dalam detik.", example = "900")
        long expiresIn,
        @Schema(description = "Ringkasan pengguna yang berhasil login.")
        ResUserSummaryDto user
) {}
