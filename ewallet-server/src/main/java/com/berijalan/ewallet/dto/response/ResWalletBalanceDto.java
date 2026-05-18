package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response saldo wallet customer.")
public record ResWalletBalanceDto(
        @Schema(description = "Saldo wallet terakhir yang terlihat oleh customer.", example = "150000")
        Long balance,
        @Schema(description = "Waktu terakhir saldo wallet diperbarui di database.", example = "2026-05-18T09:00:00")
        LocalDateTime updatedAt
) {}
