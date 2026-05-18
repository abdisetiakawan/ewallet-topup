package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.request.ReqAdminMerchantConfigDto;
import com.berijalan.ewallet.dto.request.ReqAdminMerchantTaxDto;
import com.berijalan.ewallet.dto.response.ResAdminMerchantDto;
import com.berijalan.ewallet.entity.Merchant;
import com.berijalan.ewallet.entity.MerchantTax;
import com.berijalan.ewallet.entity.constant.TaxType;
import com.berijalan.ewallet.entity.constant.TaxValueType;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.exception.NotFoundException;
import com.berijalan.ewallet.logging.LoggableAction;
import com.berijalan.ewallet.mapper.AdminMerchantMapper;
import com.berijalan.ewallet.repository.MerchantRepository;
import com.berijalan.ewallet.repository.MerchantTaxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantTaxRepository merchantTaxRepository;
    private final AdminMerchantMapper adminMerchantMapper;

    /**
     * Mengambil seluruh merchant untuk kebutuhan administrasi, termasuk yang tidak aktif.
     *
     * @return daftar merchant dan konfigurasi pajaknya.
     */
    @Transactional(readOnly = true)
    public List<ResAdminMerchantDto> getAllMerchants() {
        return merchantRepository.findAll().stream()
                .map(adminMerchantMapper::toDto)
                .toList();
    }

    /**
     * Mengambil detail merchant untuk layar administrasi.
     *
     * @param id ID merchant.
     * @return detail merchant dan konfigurasi pajaknya.
     * @throws NotFoundException jika merchant tidak ditemukan.
     */
    @Transactional(readOnly = true)
    public ResAdminMerchantDto getMerchant(Long id) {
        return adminMerchantMapper.toDto(findMerchant(id));
    }

    /**
     * Membuat merchant baru beserta konfigurasi pajaknya.
     *
     * @param request konfigurasi merchant dan pajak.
     * @return merchant yang berhasil dibuat.
     * @throws BadRequestException jika konfigurasi pajak tidak valid.
     */
    // WHY: Perubahan merchant harus langsung menghapus cache daftar merchant aktif yang dipakai customer.
    @CacheEvict(value = "merchants:active", key = "'all'")
    @Transactional
    @LoggableAction(action = "admin.create-merchant")
    public ResAdminMerchantDto createMerchant(ReqAdminMerchantConfigDto request) {
        validateConfig(request);

        Merchant merchant = new Merchant();
        merchant.setName(request.name());
        merchant.setIsActive(request.isActive());
        merchantRepository.save(merchant);

        upsertTaxes(merchant, List.of(), normalizeTaxes(request));

        return adminMerchantMapper.toDto(merchant, merchantTaxRepository.findByMerchantId(merchant.getId()));
    }

    /**
     * Memperbarui merchant dan melakukan upsert daftar pajak dalam satu transaksi.
     *
     * @param id ID merchant yang diperbarui.
     * @param request konfigurasi merchant dan pajak terbaru.
     * @return merchant setelah update.
     * @throws BadRequestException jika pajak tidak valid atau tidak dimiliki merchant tersebut.
     * @throws NotFoundException jika merchant tidak ditemukan.
     */
    // WHY: Perubahan status atau pajak merchant memengaruhi daftar merchant dan biaya pembayaran customer.
    @CacheEvict(value = "merchants:active", key = "'all'")
    @Transactional
    @LoggableAction(action = "admin.update-merchant")
    public ResAdminMerchantDto updateMerchant(Long id, ReqAdminMerchantConfigDto request) {
        validateConfig(request);

        Merchant merchant = findMerchant(id);
        merchant.setName(request.name());
        merchant.setIsActive(request.isActive());

        List<MerchantTax> existingTaxes = merchantTaxRepository.findByMerchantId(id);
        upsertTaxes(merchant, existingTaxes, normalizeTaxes(request));

        return adminMerchantMapper.toDto(merchant, merchantTaxRepository.findByMerchantId(id));
    }

    private Merchant findMerchant(Long id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Merchant not found"));
    }

    private void upsertTaxes(
            Merchant merchant,
            List<MerchantTax> existingTaxes,
            List<ReqAdminMerchantTaxDto> requestedTaxes
    ) {
        // WHY: Daftar request dianggap sebagai state final agar pajak yang dihapus admin tidak tetap aktif diam-diam.
        Map<Long, MerchantTax> existingById = existingTaxes.stream()
                .filter(tax -> tax.getId() != null)
                .collect(Collectors.toMap(MerchantTax::getId, Function.identity()));

        Set<Long> requestedIds = requestedTaxes.stream()
                .map(ReqAdminMerchantTaxDto::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<MerchantTax> removedTaxes = existingTaxes.stream()
                .filter(tax -> !requestedIds.contains(tax.getId()))
                .toList();

        List<MerchantTax> savedTaxes = new ArrayList<>();
        for (ReqAdminMerchantTaxDto taxRequest : requestedTaxes) {
            MerchantTax tax = resolveTax(merchant, existingById, taxRequest.id());
            tax.setMerchant(merchant);
            tax.setTaxName(taxRequest.taxName());
            tax.setTaxType(taxRequest.taxType());
            tax.setValueType(taxRequest.valueType());
            tax.setTaxValue(taxRequest.taxValue());
            tax.setIsActive(taxRequest.isActive());
            tax.setEffectiveAt(taxRequest.effectiveAt());
            tax.setExpiredAt(taxRequest.expiredAt());
            savedTaxes.add(tax);
        }

        if (!removedTaxes.isEmpty()) {
            merchantTaxRepository.deleteAll(removedTaxes);
        }

        merchantTaxRepository.saveAll(savedTaxes);
    }

    private MerchantTax resolveTax(
            Merchant merchant,
            Map<Long, MerchantTax> existingById,
            Long taxId
    ) {
        if (taxId == null) {
            return new MerchantTax();
        }

        MerchantTax tax = existingById.get(taxId);
        if (tax == null) {
            throw new BadRequestException("Tax does not belong to merchant " + merchant.getId());
        }

        return tax;
    }

    private void validateConfig(ReqAdminMerchantConfigDto request) {
        List<ReqAdminMerchantTaxDto> taxes = normalizeTaxes(request);
        EnumSet<TaxType> activeTypes = EnumSet.noneOf(TaxType.class);

        for (ReqAdminMerchantTaxDto tax : taxes) {
            if (tax.expiredAt() != null && !tax.expiredAt().isAfter(tax.effectiveAt())) {
                log.warn("Admin merchant config rejected because tax expiry is not after effective date. taxType={}, valueType={}",
                        tax.taxType(), tax.valueType());
                throw new BadRequestException("Tax expiry date must be after effective date");
            }

            if (tax.valueType() == TaxValueType.PERCENTAGE
                    && tax.taxValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Percentage tax value must not exceed 100");
            }

            // WHY: Satu tipe pajak aktif menjaga perhitungan pembayaran deterministik dan mudah diaudit.
            if (tax.isActive() && !activeTypes.add(tax.taxType())) {
                throw new BadRequestException("Only one active tax is allowed for each tax type");
            }
        }
    }

    private List<ReqAdminMerchantTaxDto> normalizeTaxes(ReqAdminMerchantConfigDto request) {
        return request.taxes() == null ? List.of() : request.taxes();
    }

}
