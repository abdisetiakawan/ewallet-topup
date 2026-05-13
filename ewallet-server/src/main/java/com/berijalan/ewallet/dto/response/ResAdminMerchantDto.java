package com.berijalan.ewallet.dto.response;

import java.util.List;

public record ResAdminMerchantDto(
        Long id,
        String name,
        Boolean isActive,
        List<ResAdminMerchantTaxDto> taxes
) {}
