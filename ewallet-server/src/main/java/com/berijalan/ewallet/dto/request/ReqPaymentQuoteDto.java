package com.berijalan.ewallet.dto.request;

import com.berijalan.ewallet.common.TransactionAmountLimits;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload preview pembayaran merchant sebelum transaksi dibuat.")
public record ReqPaymentQuoteDto(
        @Schema(description = "Nama merchant tujuan pembayaran.", example = "Gopay", maxLength = 255)
        @NotBlank(message = "Merchant name must not be empty")
        @Size(max = 255, message = "Merchant name maximum length is 255 characters")
        String merchantName,

        @Schema(description = "Nominal dasar pembayaran sebelum pajak merchant.", example = "100000", minimum = "10000", maximum = "10000000")
        @NotNull(message = "Payment amount is required")
        @Min(value = TransactionAmountLimits.MIN_TRANSACTION_AMOUNT, message = "Minimum payment amount is 10000")
        @Max(value = TransactionAmountLimits.MAX_PAYMENT_AMOUNT, message = "Maximum payment amount is 10000000")
        Long amount
) {}
