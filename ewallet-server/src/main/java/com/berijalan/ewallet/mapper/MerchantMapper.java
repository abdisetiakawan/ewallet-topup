package com.berijalan.ewallet.mapper;

import com.berijalan.ewallet.dto.response.ResMerchantDto;
import com.berijalan.ewallet.dto.response.ResMerchantTaxDto;
import com.berijalan.ewallet.entity.Merchant;
import com.berijalan.ewallet.entity.MerchantTax;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MerchantMapper implements BaseMapper<Merchant, ResMerchantDto> {

    public List<ResMerchantDto> toActiveDtos(List<Merchant> merchants) {
        return merchants.stream()
                .filter(merchant -> Boolean.TRUE.equals(merchant.getIsActive()))
                .map(this::toDto)
                .toList();
    }

    @Override
    public ResMerchantDto toDto(Merchant merchant) {
        return new ResMerchantDto(
                merchant.getId(),
                merchant.getName(),
                merchant.getIsActive(),
                toActiveTaxDtos(merchant.getTaxes())
        );
    }

    private List<ResMerchantTaxDto> toActiveTaxDtos(List<MerchantTax> taxes) {
        return taxes.stream()
                .filter(tax -> Boolean.TRUE.equals(tax.getIsActive()))
                .map(tax -> new ResMerchantTaxDto(
                        tax.getTaxName(),
                        tax.getTaxType().name(),
                        tax.getValueType().name(),
                        tax.getTaxValue()
                ))
                .toList();
    }
}
