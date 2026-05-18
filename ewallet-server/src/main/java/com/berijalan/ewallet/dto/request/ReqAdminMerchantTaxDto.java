package com.berijalan.ewallet.dto.request;

import com.berijalan.ewallet.entity.constant.TaxType;
import com.berijalan.ewallet.entity.constant.TaxValueType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Konfigurasi pajak merchant.")
public record ReqAdminMerchantTaxDto(
        @Schema(description = "ID pajak existing saat update. Kosong saat membuat pajak baru.", example = "1")
        Long id,

        @Schema(description = "Nama pajak yang terlihat oleh admin dan audit transaksi.", example = "Service Fee", maxLength = 100)
        @NotBlank(message = "Tax name must not be empty")
        @Size(max = 100, message = "Tax name maximum length is 100 characters")
        String taxName,

        @Schema(description = "Kategori pajak agar hanya satu pajak aktif per tipe dapat diberlakukan.", example = "SERVICE_FEE")
        @NotNull(message = "Tax type is required")
        TaxType taxType,

        @Schema(description = "Cara menghitung nilai pajak.", example = "PERCENTAGE")
        @NotNull(message = "Value type is required")
        TaxValueType valueType,

        @Schema(description = "Nilai pajak. Persentase dibatasi maksimal 100 pada service layer.", example = "1.5000", minimum = "0.0000", maximum = "1000000000.0000")
        @NotNull(message = "Tax value is required")
        @DecimalMin(value = "0.0000", message = "Tax value must not be negative")
        @DecimalMax(value = "1000000000.0000", message = "Tax value is too large")
        BigDecimal taxValue,

        @Schema(description = "Status aktif pajak untuk perhitungan pembayaran.", example = "true")
        @NotNull(message = "Tax status is required")
        Boolean isActive,

        @Schema(description = "Waktu mulai berlakunya pajak.", example = "2026-05-18T09:00:00")
        @NotNull(message = "Effective date is required")
        LocalDateTime effectiveAt,

        @Schema(description = "Waktu akhir berlakunya pajak. Kosong berarti belum ada tanggal kedaluwarsa.", example = "2026-12-31T23:59:59")
        LocalDateTime expiredAt
) {}
