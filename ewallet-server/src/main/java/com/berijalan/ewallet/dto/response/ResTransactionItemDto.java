package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Item transaksi pada riwayat customer.")
public record ResTransactionItemDto(
        @Schema(description = "ID transaksi internal.", example = "100")
        Long transactionId,
        @Schema(description = "ID customer pemilik transaksi.", example = "1")
        Long userId,
        @Schema(description = "Nama customer saat transaksi dibaca.", example = "Budi Santoso")
        String userName,
        @Schema(description = "Email customer saat transaksi dibaca.", example = "budi@example.com")
        String userEmail,
        @Schema(description = "Reference ID unik transaksi.", example = "PAY-TEST")
        String referenceId,
        @Schema(description = "Nominal final transaksi.", example = "102500")
        Long amount,
        @Schema(description = "Nominal dasar sebelum pajak.", example = "100000")
        Long baseAmount,
        @Schema(description = "Total pajak transaksi.", example = "2500")
        Long taxAmount,
        @Schema(description = "Saldo sebelum transaksi.", example = "200000")
        Long balanceBefore,
        @Schema(description = "Saldo setelah transaksi.", example = "97500")
        Long balanceAfter,
        @Schema(description = "Tipe transaksi.", example = "PAYMENT")
        String type,
        @Schema(description = "Status transaksi.", example = "SUCCESS")
        String status,
        @Schema(description = "Deskripsi transaksi dari request pembayaran.", example = "Top-up Gopay")
        String description,
        @Schema(description = "Nama merchant untuk transaksi pembayaran.", example = "Gopay")
        String merchantName,
        @Schema(description = "Waktu transaksi dibuat.", example = "2026-05-18T09:00:00")
        LocalDateTime createdAt
) {}
