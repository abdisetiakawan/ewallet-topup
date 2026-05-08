package com.berijalan.ewallet.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReqPayDto(
        @NotBlank(message = "Merchant name must not be empty")
        @Size(max = 255, message = "Merchant name maximum length is 255 characters")
        String merchantName,

        @NotNull(message = "Payment amount is required")
        @Min(value = 1, message = "Minimum payment amount is 1")
        Long amount,

        @NotBlank(message = "Payment description must not be empty")
        @Size(max = 255, message = "Payment description maximum length is 255 characters")
        String description
) {}
