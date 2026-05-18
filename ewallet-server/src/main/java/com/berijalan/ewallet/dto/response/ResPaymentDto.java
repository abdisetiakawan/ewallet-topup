package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response pembayaran merchant.")
public record ResPaymentDto(
        @Schema(description = "ID transaksi internal.", example = "20")
        Long transactionId,
        @Schema(description = "Reference ID unik transaksi pembayaran.", example = "PAY-9A1B2C3D4E")
        String referenceId,
        @Schema(description = "Total nominal yang dipotong dari wallet setelah pajak.", example = "102500")
        Long amount,
        @Schema(description = "Nominal pembayaran sebelum pajak.", example = "100000")
        Long baseAmount,
        @Schema(description = "Total pajak yang ditambahkan ke pembayaran.", example = "2500")
        Long taxAmount,
        @Schema(description = "Saldo wallet sebelum transaksi.", example = "200000")
        Long balanceBefore,
        @Schema(description = "Saldo wallet setelah transaksi.", example = "97500")
        Long balanceAfter,
        @Schema(description = "Deskripsi pembayaran.", example = "Top-up Gopay")
        String description,
        @Schema(description = "Nama merchant tujuan pembayaran.", example = "Gopay")
        String merchantName,
        @Schema(description = "Tipe transaksi.", example = "PAYMENT")
        String type,
        @Schema(description = "Status transaksi.", example = "SUCCESS")
        String status
) {}
