package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response transaksi top-up wallet.")
public record ResTopupDto(
        @Schema(description = "ID transaksi internal.", example = "10")
        Long transactionId,
        @Schema(description = "Nominal top-up yang ditambahkan ke wallet.", example = "50000")
        Long amount,
        @Schema(description = "Saldo wallet sebelum top-up.", example = "100000")
        Long balanceBefore,
        @Schema(description = "Saldo wallet setelah top-up.", example = "150000")
        Long balanceAfter,
        @Schema(description = "Tipe transaksi.", example = "TOPUP")
        String type,
        @Schema(description = "Status transaksi.", example = "SUCCESS")
        String status,
        @Schema(description = "Waktu transaksi dibuat.", example = "2026-05-18T09:00:00")
        LocalDateTime createdAt
) {}
