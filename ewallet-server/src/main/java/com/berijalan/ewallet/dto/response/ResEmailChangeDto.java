package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response permintaan perubahan email.")
public record ResEmailChangeDto(
        @Schema(description = "Email baru yang sedang menunggu konfirmasi.", example = "budi.baru@example.com")
        String newEmail,
        @Schema(description = "Masa berlaku token verifikasi dalam menit.", example = "15")
        int expiresInMinutes
) {}
