package com.berijalan.ewallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Payload konfigurasi merchant dan daftar pajaknya.")
public record ReqAdminMerchantConfigDto(
        @Schema(description = "Nama merchant yang ditampilkan kepada customer.", example = "Gopay", maxLength = 255)
        @NotBlank(message = "Merchant name must not be empty")
        @Size(max = 255, message = "Merchant name maximum length is 255 characters")
        String name,

        @Schema(description = "Status aktif merchant untuk ditampilkan pada daftar customer.", example = "true")
        @NotNull(message = "Merchant status is required")
        Boolean isActive,

        @Schema(description = "Daftar pajak merchant. Kosong berarti merchant tidak mengenakan pajak tambahan.")
        @Valid
        List<ReqAdminMerchantTaxDto> taxes
) {}
