package com.berijalan.ewallet.mapper;

import com.berijalan.ewallet.contract.model.ResAdminMerchantDto;
import com.berijalan.ewallet.contract.model.ResAdminMerchantTaxDto;
import com.berijalan.ewallet.entity.Merchant;
import com.berijalan.ewallet.entity.MerchantTax;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminMerchantMapper implements BaseMapper<Merchant, ResAdminMerchantDto> {

    @Override
    public ResAdminMerchantDto toDto(Merchant merchant) {
        return toDto(merchant, merchant.getTaxes());
    }

    public ResAdminMerchantDto toDto(Merchant merchant, List<MerchantTax> taxes) {
        return new ResAdminMerchantDto()
                .id(merchant.getId())
                .name(merchant.getName())
                .isActive(merchant.getIsActive())
                .taxes(toTaxDtos(taxes));
    }

    private List<ResAdminMerchantTaxDto> toTaxDtos(List<MerchantTax> taxes) {
        return taxes.stream()
                .map(tax -> new ResAdminMerchantTaxDto()
                        .id(tax.getId())
                        .taxName(tax.getTaxName())
                        .taxType(tax.getTaxType().name())
                        .valueType(tax.getValueType().name())
                        .taxValue(tax.getTaxValue())
                        .isActive(tax.getIsActive())
                        .effectiveAt(tax.getEffectiveAt())
                        .expiredAt(tax.getExpiredAt()))
                .toList();
    }
}
