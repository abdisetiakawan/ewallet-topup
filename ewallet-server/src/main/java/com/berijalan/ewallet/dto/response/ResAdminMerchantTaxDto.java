package com.berijalan.ewallet.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResAdminMerchantTaxDto(
        Long id,
        String taxName,
        String taxType,
        String valueType,
        BigDecimal taxValue,
        Boolean isActive,
        LocalDateTime effectiveAt,
        LocalDateTime expiredAt
) {}
