package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Response konfigurasi pajak merchant untuk admin.")
public record ResAdminMerchantTaxDto(
        @Schema(description = "ID pajak merchant.", example = "1")
        Long id,
        @Schema(description = "Nama pajak.", example = "Service Fee")
        String taxName,
        @Schema(description = "Kategori pajak.", example = "SERVICE_FEE")
        String taxType,
        @Schema(description = "Cara menghitung nilai pajak.", example = "PERCENTAGE")
        String valueType,
        @Schema(description = "Nilai pajak.", example = "1.5000")
        BigDecimal taxValue,
        @Schema(description = "Status aktif pajak.", example = "true")
        Boolean isActive,
        @Schema(description = "Waktu mulai berlaku.", example = "2026-05-18T09:00:00")
        LocalDateTime effectiveAt,
        @Schema(description = "Waktu akhir berlaku. Kosong bila belum kedaluwarsa.", example = "2026-12-31T23:59:59")
        LocalDateTime expiredAt
) {}
