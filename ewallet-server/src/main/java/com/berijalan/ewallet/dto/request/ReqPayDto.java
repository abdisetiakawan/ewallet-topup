package com.berijalan.ewallet.dto.request;

import com.berijalan.ewallet.common.TransactionAmountLimits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReqPayDto(
        @NotBlank(message = "Merchant name must not be empty")
        @Size(max = 255, message = "Merchant name maximum length is 255 characters")
        String merchantName,

        @NotNull(message = "Payment amount is required")
        @Min(value = TransactionAmountLimits.MIN_TRANSACTION_AMOUNT, message = "Minimum payment amount is 10000")
        @Max(value = TransactionAmountLimits.MAX_PAYMENT_AMOUNT, message = "Maximum payment amount is 10000000")
        Long amount,

        @NotBlank(message = "Payment description must not be empty")
        @Size(max = 255, message = "Payment description maximum length is 255 characters")
        String description
) {}
