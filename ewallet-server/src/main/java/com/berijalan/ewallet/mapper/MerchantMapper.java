package com.berijalan.ewallet.mapper;

import com.berijalan.ewallet.contract.model.ResMerchantDto;
import com.berijalan.ewallet.contract.model.ResMerchantTaxDto;
import com.berijalan.ewallet.entity.Merchant;
import com.berijalan.ewallet.entity.MerchantTax;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MerchantMapper implements BaseMapper<Merchant, ResMerchantDto> {

    public List<ResMerchantDto> toActiveDtos(List<Merchant> merchants) {
        return merchants.stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ResMerchantDto toDto(Merchant merchant) {
        return new ResMerchantDto()
                .id(merchant.getId())
                .name(merchant.getName())
                .isActive(merchant.getIsActive())
                .taxes(toActiveTaxDtos(merchant.getTaxes()));
    }

    private List<ResMerchantTaxDto> toActiveTaxDtos(List<MerchantTax> taxes) {
        return taxes.stream()
                .filter(tax -> Boolean.TRUE.equals(tax.getIsActive()))
                .map(tax -> new ResMerchantTaxDto()
                        .taxName(tax.getTaxName())
                        .taxType(tax.getTaxType().name())
                        .valueType(tax.getValueType().name())
                        .taxValue(tax.getTaxValue()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
