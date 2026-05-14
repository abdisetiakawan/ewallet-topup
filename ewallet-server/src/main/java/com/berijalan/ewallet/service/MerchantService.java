package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.response.ResMerchantDto;
import com.berijalan.ewallet.mapper.MerchantMapper;
import com.berijalan.ewallet.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;

    @Cacheable(value = "merchants:active", key = "'all'")
    @Transactional(readOnly = true)
    public List<ResMerchantDto> getAllActiveMerchants() {
        return merchantMapper.toActiveDtos(merchantRepository.findAll());
    }
}
