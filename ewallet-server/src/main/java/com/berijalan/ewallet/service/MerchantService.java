package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.response.ResMerchantDto;
import com.berijalan.ewallet.dto.response.ResMerchantTaxDto;
import com.berijalan.ewallet.entity.Merchant;
import com.berijalan.ewallet.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    @Transactional(readOnly = true)
    public List<ResMerchantDto> getAllActiveMerchants() {
        List<Merchant> merchants = merchantRepository.findAll();
        
        return merchants.stream()
                .filter(Merchant::getIsActive)
                .map(merchant -> {
                    List<ResMerchantTaxDto> taxDtos = merchant.getTaxes().stream()
                            .filter(tax -> tax.getIsActive())
                            .map(tax -> new ResMerchantTaxDto(
                                    tax.getTaxName(),
                                    tax.getTaxType().name(),
                                    tax.getValueType().name(),
                                    tax.getTaxValue()
                            ))
                            .collect(Collectors.toList());

                    return new ResMerchantDto(
                            merchant.getId(),
                            merchant.getName(),
                            merchant.getIsActive(),
                            taxDtos
                    );
                })
                .collect(Collectors.toList());
    }
}
