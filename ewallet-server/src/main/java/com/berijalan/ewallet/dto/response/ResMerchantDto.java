package com.berijalan.ewallet.dto.response;

import java.util.List;

public record ResMerchantDto(
        Long id,
        String name,
        Boolean isActive,
        List<ResMerchantTaxDto> taxes
) {}
