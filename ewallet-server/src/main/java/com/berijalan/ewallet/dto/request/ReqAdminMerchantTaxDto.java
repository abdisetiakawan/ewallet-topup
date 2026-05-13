package com.berijalan.ewallet.dto.request;

import com.berijalan.ewallet.entity.constant.TaxType;
import com.berijalan.ewallet.entity.constant.TaxValueType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReqAdminMerchantTaxDto(
        Long id,

        @NotBlank(message = "Tax name must not be empty")
        @Size(max = 100, message = "Tax name maximum length is 100 characters")
        String taxName,

        @NotNull(message = "Tax type is required")
        TaxType taxType,

        @NotNull(message = "Value type is required")
        TaxValueType valueType,

        @NotNull(message = "Tax value is required")
        @DecimalMin(value = "0.0000", message = "Tax value must not be negative")
        @DecimalMax(value = "1000000000.0000", message = "Tax value is too large")
        BigDecimal taxValue,

        @NotNull(message = "Tax status is required")
        Boolean isActive,

        @NotNull(message = "Effective date is required")
        LocalDateTime effectiveAt,

        LocalDateTime expiredAt
) {}
