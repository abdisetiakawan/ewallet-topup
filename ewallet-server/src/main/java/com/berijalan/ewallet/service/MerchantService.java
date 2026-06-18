package com.berijalan.ewallet.service;

import com.berijalan.ewallet.contract.model.ResMerchantDto;
import com.berijalan.ewallet.mapper.MerchantMapper;
import com.berijalan.ewallet.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;

    /**
     * Mengambil daftar merchant aktif untuk pilihan pembayaran customer.
     *
     * @return merchant aktif beserta pajak aktifnya.
     */
    @Cacheable(value = "merchants:active", key = "'all'")
    @Transactional(readOnly = true)
    public List<ResMerchantDto> getAllActiveMerchants() {
        // WHY: Daftar merchant aktif jarang berubah dan dievict saat admin mengubah konfigurasi merchant.
        List<ResMerchantDto> merchants = merchantMapper.toActiveDtos(merchantRepository.findByIsActiveTrue());
        return merchants;
    }
}
