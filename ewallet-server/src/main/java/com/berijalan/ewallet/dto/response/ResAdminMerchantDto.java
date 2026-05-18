package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response detail merchant untuk admin.")
public record ResAdminMerchantDto(
        @Schema(description = "ID merchant.", example = "1")
        Long id,
        @Schema(description = "Nama merchant.", example = "Gopay")
        String name,
        @Schema(description = "Status aktif merchant.", example = "true")
        Boolean isActive,
        @Schema(description = "Daftar konfigurasi pajak merchant.")
        List<ResAdminMerchantTaxDto> taxes
) {}
