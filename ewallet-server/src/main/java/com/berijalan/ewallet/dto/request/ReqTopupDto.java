package com.berijalan.ewallet.dto.request;

import com.berijalan.ewallet.common.TransactionAmountLimits;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload top-up saldo wallet.")
public record ReqTopupDto(
        @Schema(description = "Nominal top-up yang akan ditambahkan ke saldo wallet.", example = "50000", minimum = "10000", maximum = "10000000")
        @NotNull(message = "Amount is required")
        @Min(value = TransactionAmountLimits.MIN_TRANSACTION_AMOUNT, message = "Minimum top-up amount is 10000")
        @Max(value = TransactionAmountLimits.MAX_TOPUP_AMOUNT, message = "Maximum top-up amount is 10000000")
        Long amount
) {}
