package com.berijalan.ewallet.service;

import com.berijalan.ewallet.common.TransactionAmountLimits;
import com.berijalan.ewallet.contract.model.ReqPayDto;
import com.berijalan.ewallet.contract.model.ReqPaymentQuoteDto;
import com.berijalan.ewallet.contract.model.ResPaymentDto;
import com.berijalan.ewallet.contract.model.ResPaymentQuoteDto;
import com.berijalan.ewallet.contract.model.ResTransactionDetailDto;
import com.berijalan.ewallet.contract.model.ResTransactionHistoryDto;
import com.berijalan.ewallet.contract.model.TaxSnapshotDto;
import com.berijalan.ewallet.entity.Merchant;
import com.berijalan.ewallet.entity.MerchantTax;
import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.exception.NotFoundException;
import com.berijalan.ewallet.mapper.TransactionMapper;
import com.berijalan.ewallet.repository.MerchantRepository;
import com.berijalan.ewallet.repository.MerchantTaxRepository;
import com.berijalan.ewallet.repository.TransactionRepository;
import com.berijalan.ewallet.repository.WalletRepository;
import com.berijalan.ewallet.util.ReferenceIdGenerator;
import com.berijalan.ewallet.logging.LoggableAction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantTaxRepository merchantTaxRepository;
    private final ObjectMapper objectMapper;
    private final WalletCacheService walletCacheService;
    private final TaxCalculator taxCalculator;
    private final TransactionMapper transactionMapper;

    /**
     * Memproses pembayaran merchant dengan pemotongan saldo, perhitungan pajak aktif, dan audit balance.
     *
     * @param request detail merchant, nominal dasar, dan deskripsi pembayaran.
     * @param userId ID customer pemilik wallet.
     * @return detail pembayaran yang berhasil dibuat.
     * @throws BadRequestException jika amount tidak valid atau saldo tidak mencukupi.
     * @throws NotFoundException jika wallet atau merchant tidak ditemukan.
     */
    @Transactional
    @LoggableAction(action = "transaction.pay", logSuccess = false)
    public ResPaymentDto pay(ReqPayDto request, Long userId) {
        validatePaymentAmount(request.getAmount(), userId, request.getMerchantName());

        // WHY: Pembayaran harus serial per wallet agar dua request paralel tidak menghasilkan saldo negatif.
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> {
                    log.warn("Payment rejected because wallet was not found. userId={}", userId);
                    return new NotFoundException("Wallet not found");
                });
        User user = wallet.getUser();

        Merchant merchant = findMerchant(request.getMerchantName(), userId);
        PaymentCalculation paymentCalculation = calculatePayment(merchant, request.getAmount(), userId);
        long baseAmount = paymentCalculation.baseAmount();
        long totalTax = paymentCalculation.totalTax();
        long finalAmount = paymentCalculation.finalAmount();

        if (wallet.getBalance() < finalAmount) {
            log.warn("Payment rejected because balance is insufficient. userId={}, merchant={}, baseAmount={}, tax={}, finalAmount={}, balance={}",
                    userId, merchant.getName(), baseAmount, totalTax, finalAmount, wallet.getBalance());
            throw new BadRequestException("Insufficient balance");
        }

        long balanceBefore = wallet.getBalance();
        long balanceAfter = balanceBefore - finalAmount;
        wallet.setBalance(balanceAfter);

        Transaction transaction = createPaymentTransaction(
                user,
                merchant,
                request,
                finalAmount,
                totalTax,
                balanceBefore,
                balanceAfter,
                // WHY: Snapshot pajak mempertahankan audit transaksi saat konfigurasi pajak merchant berubah.
                serializeTaxSnapshots(paymentCalculation.taxSnapshots())
        );

        transaction = transactionRepository.saveAndFlush(transaction);
        // WHY: Cache saldo diperbarui setelah commit agar pembacaan berikutnya tidak melihat nilai yang rollback.
        walletCacheService.putAfterCommit(userId, balanceAfter, wallet.getUpdatedAt());

        log.info("Payment success. userId={}, merchant={}, baseAmount={}, tax={}, finalAmount={}, balanceBefore={}, balanceAfter={}, referenceId={}",
                userId, merchant.getName(), baseAmount, totalTax, finalAmount, balanceBefore, balanceAfter, transaction.getReferenceId());

        return transactionMapper.toPaymentDto(transaction);
    }

    /**
     * Menghitung preview pembayaran berdasarkan pajak aktif merchant saat ini.
     *
     * @param request merchant dan nominal dasar yang akan dipreview.
     * @return nominal final dan rincian pajak yang dihitung.
     */
    @Transactional(readOnly = true)
    public ResPaymentQuoteDto quotePayment(ReqPaymentQuoteDto request) {
        validatePaymentAmount(request.getAmount(), null, request.getMerchantName());

        Merchant merchant = findMerchant(request.getMerchantName(), null);
        PaymentCalculation paymentCalculation = calculatePayment(merchant, request.getAmount(), null);

        return new ResPaymentQuoteDto()
                .merchantName(merchant.getName())
                .baseAmount(paymentCalculation.baseAmount())
                .taxAmount(paymentCalculation.totalTax())
                .amount(paymentCalculation.finalAmount())
                .taxDetails(toTaxSnapshotDtos(paymentCalculation.taxSnapshots()));
    }

    private void validatePaymentAmount(Long amount, Long userId, String merchantName) {
        if (amount == null) {
            log.warn("Payment rejected because amount is missing. userId={}, merchant={}", userId, merchantName);
            throw new BadRequestException("Payment amount is required");
        }

        if (amount < TransactionAmountLimits.MIN_TRANSACTION_AMOUNT) {
            log.warn("Payment rejected because amount is below minimum. userId={}, merchant={}, amount={}, minimum={}",
                    userId, merchantName, amount, TransactionAmountLimits.MIN_TRANSACTION_AMOUNT);
            throw new BadRequestException("Minimum payment amount is 10000");
        }

        if (amount > TransactionAmountLimits.MAX_PAYMENT_AMOUNT) {
            log.warn("Payment rejected because amount exceeds maximum. userId={}, merchant={}, amount={}, maximum={}",
                    userId, merchantName, amount, TransactionAmountLimits.MAX_PAYMENT_AMOUNT);
            throw new BadRequestException("Maximum payment amount is 10000000");
        }
    }

    private long safeAddPaymentAmount(long baseAmount, long totalTax, Long userId, String merchantName) {
        try {
            return Math.addExact(baseAmount, totalTax);
        } catch (ArithmeticException ex) {
            log.error("Payment failed because final amount overflowed. userId={}, merchant={}, baseAmount={}, totalTax={}",
                    userId, merchantName, baseAmount, totalTax, ex);
            throw new BadRequestException("Payment amount limit exceeded");
        }
    }

    private Merchant findMerchant(String merchantName, Long userId) {
        return merchantRepository.findByName(merchantName)
                .orElseThrow(() -> {
                    log.warn("Payment rejected because merchant was not found. merchantName={}, userId={}",
                            merchantName, userId);
                    return new NotFoundException("Merchant not found");
                });
    }

    private PaymentCalculation calculatePayment(Merchant merchant, long baseAmount, Long userId) {
        List<MerchantTax> activeTaxes = merchantTaxRepository.findByMerchantIdAndIsActiveTrue(merchant.getId());
        TaxCalculator.TaxCalculationResult taxCalculation = taxCalculator.calculate(baseAmount, activeTaxes);
        long totalTax = taxCalculation.totalTax();

        // WHY: Batas maksimum diterapkan pada nominal final karena pajak ikut dipotong dari saldo customer.
        long finalAmount = safeAddPaymentAmount(baseAmount, totalTax, userId, merchant.getName());
        validateFinalPaymentAmount(finalAmount, userId, merchant.getName());

        return new PaymentCalculation(baseAmount, totalTax, finalAmount, taxCalculation.snapshots());
    }

    private void validateFinalPaymentAmount(long finalAmount, Long userId, String merchantName) {
        if (finalAmount > TransactionAmountLimits.MAX_PAYMENT_AMOUNT) {
            log.warn("Payment rejected because final amount exceeds maximum. userId={}, merchant={}, finalAmount={}, maximum={}",
                    userId, merchantName, finalAmount, TransactionAmountLimits.MAX_PAYMENT_AMOUNT);
            throw new BadRequestException("Maximum payment amount is 10000000");
        }
    }

    private Transaction createPaymentTransaction(
            User user,
            Merchant merchant,
            ReqPayDto request,
            long finalAmount,
            long totalTax,
            long balanceBefore,
            long balanceAfter,
            String taxSnapshotJson
    ) {
        Transaction transaction = new Transaction();
        transaction.setReferenceId(ReferenceIdGenerator.generate("PAY-"));
        transaction.setAmount(finalAmount);
        transaction.setBaseAmount(request.getAmount());
        transaction.setTaxAmount(totalTax);
        transaction.setTaxSnapshot(taxSnapshotJson);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(request.getDescription());
        transaction.setType(TransactionType.PAYMENT);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setMerchant(merchant);
        transaction.setUser(user);
        return transaction;
    }

    private String serializeTaxSnapshots(List<TaxCalculator.TaxSnapshot> taxSnapshots) {
        if (taxSnapshots.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(taxSnapshots);
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize payment tax snapshot", ex);
            throw new IllegalStateException("Failed to serialize tax snapshot", ex);
        }
    }

    private List<TaxSnapshotDto> toTaxSnapshotDtos(List<TaxCalculator.TaxSnapshot> taxSnapshots) {
        return taxSnapshots.stream()
                .map(taxSnapshot -> new TaxSnapshotDto()
                        .taxName(taxSnapshot.taxName())
                        .taxCategory(taxSnapshot.taxType())
                        .valueType(taxSnapshot.valueType())
                        .taxValue(taxSnapshot.taxValue())
                        .calculatedAmount(taxSnapshot.calculatedTax()))
                .toList();
    }

    /**
     * Mengambil riwayat transaksi customer dengan filter opsional.
     *
     * @param userId ID customer pemilik transaksi.
     * @param request filter status, tipe, dan pagination.
     * @return halaman riwayat transaksi yang sudah dipetakan untuk API.
     */
    @Transactional(readOnly = true)
    public ResTransactionHistoryDto getTransactions(
            Long userId,
            Integer page,
            Integer size,
            com.berijalan.ewallet.contract.model.TransactionStatus status,
            com.berijalan.ewallet.contract.model.TransactionType type
    ) {
        Pageable pageable = PageRequest.of(
                page == null ? 0 : page,
                size == null ? 10 : size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<Transaction> transactionPage = findTransactionPage(
                userId,
                toEntityTransactionStatus(status),
                toEntityTransactionType(type),
                pageable
        );

        return transactionMapper.toHistoryDto(transactionPage);
    }

    /**
     * Mengambil detail transaksi yang hanya boleh dibaca pemiliknya.
     *
     * @param userId ID customer pemilik transaksi.
     * @param transactionId ID transaksi yang diminta.
     * @return detail transaksi beserta snapshot pajak pembayaran.
     * @throws NotFoundException jika transaksi tidak ditemukan atau bukan milik customer.
     */
    @Transactional(readOnly = true)
    public ResTransactionDetailDto getTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        return transactionMapper.toDetailDto(transaction, deserializeTaxSnapshots(transaction.getTaxSnapshot()));
    }

    private Page<Transaction> findTransactionPage(
            Long userId,
            TransactionStatus status,
            TransactionType type,
            Pageable pageable
    ) {
        if (status != null && type != null) {
            return transactionRepository.findByUserIdAndStatusAndType(userId, status, type, pageable);
        }

        if (status != null) {
            return transactionRepository.findByUserIdAndStatus(userId, status, pageable);
        }

        if (type != null) {
            return transactionRepository.findByUserIdAndType(userId, type, pageable);
        }

        return transactionRepository.findByUserId(userId, pageable);
    }

    private List<TaxSnapshotDto> deserializeTaxSnapshots(String taxSnapshotJson) {
        if (taxSnapshotJson == null || taxSnapshotJson.isBlank()) {
            return List.of();
        }

        try {
            List<StoredTaxSnapshot> snapshots = objectMapper.readValue(
                    taxSnapshotJson,
                    new TypeReference<List<StoredTaxSnapshot>>() {
                    }
            );

            return snapshots.stream()
                    .map(StoredTaxSnapshot::toDto)
                    .toList();
        } catch (JsonProcessingException ex) {
            log.error("Failed to deserialize transaction tax snapshot", ex);
            throw new IllegalStateException("Failed to deserialize tax snapshot", ex);
        }
    }

    private record StoredTaxSnapshot(
            String taxName,
            String taxType,
            String valueType,
            BigDecimal taxValue,
            Long calculatedTax
    ) {
        private TaxSnapshotDto toDto() {
            return new TaxSnapshotDto()
                    .taxName(taxName)
                    .taxCategory(taxType)
                    .valueType(valueType)
                    .taxValue(taxValue)
                    .calculatedAmount(calculatedTax);
        }
    }

    private TransactionStatus toEntityTransactionStatus(
            com.berijalan.ewallet.contract.model.TransactionStatus status
    ) {
        return status == null ? null : TransactionStatus.valueOf(status.getValue());
    }

    private TransactionType toEntityTransactionType(
            com.berijalan.ewallet.contract.model.TransactionType type
    ) {
        return type == null ? null : TransactionType.valueOf(type.getValue());
    }

    private record PaymentCalculation(
            long baseAmount,
            long totalTax,
            long finalAmount,
            List<TaxCalculator.TaxSnapshot> taxSnapshots
    ) {
    }

}
