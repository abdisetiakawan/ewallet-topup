package com.berijalan.ewallet.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReqAdminMerchantConfigDto(
        @NotBlank(message = "Merchant name must not be empty")
        @Size(max = 255, message = "Merchant name maximum length is 255 characters")
        String name,

        @NotNull(message = "Merchant status is required")
        Boolean isActive,

        @Valid
        List<ReqAdminMerchantTaxDto> taxes
) {}
