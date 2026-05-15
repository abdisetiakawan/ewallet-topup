package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.response.ResMerchantDto;
import com.berijalan.ewallet.entity.Merchant;
import com.berijalan.ewallet.mapper.MerchantMapper;
import com.berijalan.ewallet.repository.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Spy
    private MerchantMapper merchantMapper = new MerchantMapper();

    @InjectMocks
    private MerchantService merchantService;

    @Test
    void getAllActiveMerchants_shouldQueryOnlyActiveMerchants() {
        Merchant activeMerchant = new Merchant();
        activeMerchant.setId(1L);
        activeMerchant.setName("Gopay");
        activeMerchant.setIsActive(true);

        when(merchantRepository.findByIsActiveTrue()).thenReturn(List.of(activeMerchant));

        List<ResMerchantDto> result = merchantService.getAllActiveMerchants();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Gopay");
        assertThat(result.get(0).isActive()).isTrue();
        verify(merchantRepository).findByIsActiveTrue();
    }
}
