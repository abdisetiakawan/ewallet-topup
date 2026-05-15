package com.berijalan.ewallet.mapper;

import com.berijalan.ewallet.dto.response.ResAdminMerchantDto;
import com.berijalan.ewallet.dto.response.ResAdminMerchantTaxDto;
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
        return new ResAdminMerchantDto(
                merchant.getId(),
                merchant.getName(),
                merchant.getIsActive(),
                toTaxDtos(taxes)
        );
    }

    private List<ResAdminMerchantTaxDto> toTaxDtos(List<MerchantTax> taxes) {
        return taxes.stream()
                .map(tax -> new ResAdminMerchantTaxDto(
                        tax.getId(),
                        tax.getTaxName(),
                        tax.getTaxType().name(),
                        tax.getValueType().name(),
                        tax.getTaxValue(),
                        tax.getIsActive(),
                        tax.getEffectiveAt(),
                        tax.getExpiredAt()
                ))
                .toList();
    }
}
