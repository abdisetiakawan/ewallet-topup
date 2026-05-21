package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Detail transaksi customer untuk kebutuhan audit riwayat.")
public record ResTransactionDetailDto(
        @Schema(description = "ID transaksi internal.", example = "100")
        Long transactionId,
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
        @Schema(description = "Snapshot rincian pajak transaksi pembayaran.")
        List<TaxSnapshotDto> taxDetails,
        @Schema(description = "Waktu transaksi dibuat.", example = "2026-05-18T09:00:00")
        LocalDateTime createdAt
) {}
