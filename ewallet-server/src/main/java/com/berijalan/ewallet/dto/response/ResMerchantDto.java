package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response merchant aktif untuk customer.")
public record ResMerchantDto(
        @Schema(description = "ID merchant.", example = "1")
        Long id,
        @Schema(description = "Nama merchant.", example = "Gopay")
        String name,
        @Schema(description = "Status aktif merchant.", example = "true")
        Boolean isActive,
        @Schema(description = "Daftar pajak aktif merchant.")
        List<ResMerchantTaxDto> taxes
) {}
