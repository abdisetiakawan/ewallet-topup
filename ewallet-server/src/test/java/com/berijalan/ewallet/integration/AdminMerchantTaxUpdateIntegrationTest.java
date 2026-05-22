package com.berijalan.ewallet.integration;

import com.berijalan.ewallet.dto.request.ReqAdminMerchantConfigDto;
import com.berijalan.ewallet.dto.request.ReqAdminMerchantTaxDto;
import com.berijalan.ewallet.dto.response.ResAdminMerchantDto;
import com.berijalan.ewallet.entity.MerchantTax;
import com.berijalan.ewallet.entity.constant.TaxType;
import com.berijalan.ewallet.entity.constant.TaxValueType;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.repository.MerchantTaxRepository;
import com.berijalan.ewallet.service.AdminMerchantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.data.redis.repositories.enabled=false",
        "spring.flyway.ignore-migration-patterns=*:missing",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate.SQL=OFF"
})
@Transactional
class AdminMerchantTaxUpdateIntegrationTest {

    @MockitoBean(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private CacheManager cacheManager;

    @Autowired
    private AdminMerchantService adminMerchantService;

    @Autowired
    private MerchantTaxRepository merchantTaxRepository;

    @BeforeEach
    void stubMerchantCache() {
        when(cacheManager.getCache(anyString())).thenAnswer(invocation ->
                new ConcurrentMapCache(invocation.getArgument(0, String.class)));
    }

    @Test
    void updateMerchant_whenExistingTaxIsRetagged_shouldAllowNewTaxForPreviousType() {
        ResAdminMerchantDto merchant = createMerchantWithProcessingFee();
        Long existingTaxId = merchant.taxes().get(0).id();

        ResAdminMerchantDto updated = adminMerchantService.updateMerchant(
                merchant.id(),
                config(
                        merchant.name(),
                        tax(existingTaxId, "Service Fee", TaxType.SERVICE_FEE, TaxValueType.PERCENTAGE, "1.5000"),
                        tax(null, "Replacement Processing Fee", TaxType.PROCESSING_FEE, TaxValueType.FIXED, "1500.0000")
                )
        );

        assertThat(updated.taxes()).hasSize(2);
        assertThat(activeTypes(updated.id())).containsExactlyInAnyOrder(
                TaxType.SERVICE_FEE,
                TaxType.PROCESSING_FEE
        );
    }

    @Test
    void updateMerchant_whenActiveTaxIsRemoved_shouldAllowReplacementWithSameType() {
        ResAdminMerchantDto merchant = createMerchantWithProcessingFee();

        ResAdminMerchantDto updated = adminMerchantService.updateMerchant(
                merchant.id(),
                config(
                        merchant.name(),
                        tax(null, "Replacement Processing Fee", TaxType.PROCESSING_FEE, TaxValueType.FIXED, "2000.0000")
                )
        );

        assertThat(updated.taxes()).hasSize(1);
        assertThat(updated.taxes().get(0).id()).isNotEqualTo(merchant.taxes().get(0).id());
        assertThat(activeTypes(updated.id())).containsExactly(TaxType.PROCESSING_FEE);
    }

    @Test
    void updateMerchant_whenFinalPayloadHasDuplicateActiveTypes_shouldRejectDomainRequest() {
        ResAdminMerchantDto merchant = createMerchantWithProcessingFee();
        Long existingTaxId = merchant.taxes().get(0).id();

        assertThatThrownBy(() -> adminMerchantService.updateMerchant(
                merchant.id(),
                config(
                        merchant.name(),
                        tax(existingTaxId, "Processing Fee", TaxType.PROCESSING_FEE, TaxValueType.FIXED, "1000.0000"),
                        tax(null, "Duplicate Processing Fee", TaxType.PROCESSING_FEE, TaxValueType.FIXED, "1500.0000")
                )
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only one active tax is allowed for each tax type");
    }

    private ResAdminMerchantDto createMerchantWithProcessingFee() {
        return adminMerchantService.createMerchant(config(
                "Merchant " + UUID.randomUUID(),
                tax(null, "Processing Fee", TaxType.PROCESSING_FEE, TaxValueType.FIXED, "1000.0000")
        ));
    }

    private ReqAdminMerchantConfigDto config(String merchantName, ReqAdminMerchantTaxDto... taxes) {
        return new ReqAdminMerchantConfigDto(merchantName, true, List.of(taxes));
    }

    private ReqAdminMerchantTaxDto tax(
            Long id,
            String name,
            TaxType taxType,
            TaxValueType valueType,
            String value
    ) {
        return new ReqAdminMerchantTaxDto(
                id,
                name,
                taxType,
                valueType,
                new BigDecimal(value),
                true,
                LocalDateTime.of(2026, 5, 22, 0, 0),
                null
        );
    }

    private Set<TaxType> activeTypes(Long merchantId) {
        return merchantTaxRepository.findByMerchantId(merchantId).stream()
                .filter(tax -> Boolean.TRUE.equals(tax.getIsActive()))
                .map(MerchantTax::getTaxType)
                .collect(Collectors.toSet());
    }
}
