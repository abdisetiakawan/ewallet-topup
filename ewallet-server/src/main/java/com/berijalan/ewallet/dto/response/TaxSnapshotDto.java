package com.berijalan.ewallet.dto.response;

import java.math.BigDecimal;

public record TaxSnapshotDto(
        String taxName,
        String taxCategory,
        String valueType,
        BigDecimal taxValue,
        Long calculatedAmount
) {}
