package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Pajak aktif merchant yang digunakan saat pembayaran.")
public record ResMerchantTaxDto(
        @Schema(description = "Nama pajak.", example = "Service Fee")
        String taxName,
        @Schema(description = "Kategori pajak.", example = "SERVICE_FEE")
        String taxType,
        @Schema(description = "Cara menghitung nilai pajak.", example = "PERCENTAGE")
        String valueType,
        @Schema(description = "Nilai pajak aktif.", example = "1.5000")
        BigDecimal taxValue
) {}
