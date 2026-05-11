package com.berijalan.ewallet.dto.response;

import java.math.BigDecimal;

public record ResMerchantTaxDto(
        String taxName,
        String taxType,
        String valueType,
        BigDecimal taxValue
) {}
