package com.berijalan.ewallet.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReqPayDto(
        @NotBlank(message = "Merchant name must not be empty")
        String merchantName,

        @NotNull(message = "Payment amount is required")
        @Min(value = 1, message = "Minimum payment amount is 1")
        Long amount
) {}