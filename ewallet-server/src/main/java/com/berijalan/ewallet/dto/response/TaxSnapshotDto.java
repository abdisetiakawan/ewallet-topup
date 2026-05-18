package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Snapshot pajak yang disimpan bersama transaksi pembayaran.")
public record TaxSnapshotDto(
        @Schema(description = "Nama pajak saat transaksi terjadi.", example = "Service Fee")
        String taxName,
        @Schema(description = "Kategori pajak saat transaksi terjadi.", example = "SERVICE_FEE")
        String taxCategory,
        @Schema(description = "Cara menghitung pajak saat transaksi terjadi.", example = "PERCENTAGE")
        String valueType,
        @Schema(description = "Nilai pajak saat transaksi terjadi.", example = "1.5000")
        BigDecimal taxValue,
        @Schema(description = "Nominal pajak hasil perhitungan.", example = "1500")
        Long calculatedAmount
) {}
