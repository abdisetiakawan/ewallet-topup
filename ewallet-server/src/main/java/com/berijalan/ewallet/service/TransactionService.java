package com.berijalan.ewallet.service;

import com.berijalan.ewallet.common.TransactionAmountLimits;
import com.berijalan.ewallet.dto.request.ReqPayDto;
import com.berijalan.ewallet.dto.request.ReqTransactionHistoryDto;
import com.berijalan.ewallet.dto.response.ResPaymentDto;
import com.berijalan.ewallet.dto.response.ResTransactionHistoryDto;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    @LoggableAction(action = "transaction.pay", logSuccess = false)
    public ResPaymentDto pay(ReqPayDto request, Long userId) {
        validatePaymentAmount(request.amount());

        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> {
                    log.warn("Payment rejected because wallet was not found. userId={}", userId);
                    return new NotFoundException("Wallet not found");
                });
        User user = wallet.getUser();

        Merchant merchant = merchantRepository.findByName(request.merchantName())
                .orElseThrow(() -> {
                    log.warn("Payment rejected because merchant was not found. merchantName={}, userId={}",
                            request.merchantName(), userId);
                    return new NotFoundException("Merchant not found");
                });

        List<MerchantTax> activeTaxes = merchantTaxRepository.findByMerchantIdAndIsActiveTrue(merchant.getId());
        long baseAmount = request.amount();
        TaxCalculator.TaxCalculationResult taxCalculation = taxCalculator.calculate(baseAmount, activeTaxes);
        long totalTax = taxCalculation.totalTax();
        long finalAmount = safeAddPaymentAmount(baseAmount, totalTax);
        validateFinalPaymentAmount(finalAmount);

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
                serializeTaxSnapshots(taxCalculation.snapshots())
        );

        transaction = transactionRepository.saveAndFlush(transaction);
        walletCacheService.putAfterCommit(userId, balanceAfter, wallet.getUpdatedAt());

        log.info("Payment success. userId={}, merchant={}, baseAmount={}, tax={}, finalAmount={}, referenceId={}",
                userId, merchant.getName(), baseAmount, totalTax, finalAmount, transaction.getReferenceId());

        return transactionMapper.toPaymentDto(transaction);
    }

    private void validatePaymentAmount(Long amount) {
        if (amount == null) {
            throw new BadRequestException("Payment amount is required");
        }

        if (amount < TransactionAmountLimits.MIN_TRANSACTION_AMOUNT) {
            throw new BadRequestException("Minimum payment amount is 10000");
        }

        if (amount > TransactionAmountLimits.MAX_PAYMENT_AMOUNT) {
            throw new BadRequestException("Maximum payment amount is 10000000");
        }
    }

    private long safeAddPaymentAmount(long baseAmount, long totalTax) {
        try {
            return Math.addExact(baseAmount, totalTax);
        } catch (ArithmeticException ex) {
            throw new BadRequestException("Payment amount limit exceeded");
        }
    }

    private void validateFinalPaymentAmount(long finalAmount) {
        if (finalAmount > TransactionAmountLimits.MAX_PAYMENT_AMOUNT) {
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
        transaction.setBaseAmount(request.amount());
        transaction.setTaxAmount(totalTax);
        transaction.setTaxSnapshot(taxSnapshotJson);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(request.description());
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
            throw new IllegalStateException("Failed to serialize tax snapshot", ex);
        }
    }

    @Transactional(readOnly = true)
    public ResTransactionHistoryDto getTransactions(Long userId, ReqTransactionHistoryDto request) {
        Pageable pageable = request.toPageable(Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> transactionPage = findTransactionPage(userId, request, pageable);

        return transactionMapper.toHistoryDto(transactionPage);
    }

    private Page<Transaction> findTransactionPage(Long userId, ReqTransactionHistoryDto request, Pageable pageable) {
        TransactionStatus status = request.status();
        TransactionType type = request.type();

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

}
