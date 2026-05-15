package com.berijalan.ewallet.dto.request;

import com.berijalan.ewallet.common.TransactionAmountLimits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReqTopupDto(
        @NotNull(message = "Amount is required")
        @Min(value = TransactionAmountLimits.MIN_TRANSACTION_AMOUNT, message = "Minimum top-up amount is 10000")
        @Max(value = TransactionAmountLimits.MAX_TOPUP_AMOUNT, message = "Maximum top-up amount is 10000000")
        Long amount
) {}
