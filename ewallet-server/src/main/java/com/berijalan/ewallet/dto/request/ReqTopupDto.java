package com.berijalan.ewallet.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReqTopupDto(
        @NotNull(message = "Amount is required")
        @Min(value = 1, message = "Amount must be greater than 0")
        Long amount
) {}
