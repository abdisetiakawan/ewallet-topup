package com.berijalan.ewallet.service;

import java.math.BigDecimal;

public record TaxSnapshot(
        String taxName,
        String taxType,
        String valueType,
        BigDecimal taxValue,
        long calculatedTax
) {}
