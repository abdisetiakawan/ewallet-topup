package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.request.ReqPayDto;
import com.berijalan.ewallet.dto.request.ReqTransactionHistoryDto;
import com.berijalan.ewallet.dto.response.ResPaymentDto;
import com.berijalan.ewallet.dto.response.ResTransactionHistoryDto;
import com.berijalan.ewallet.dto.response.ResTransactionItemDto;
import com.berijalan.ewallet.dto.response.TaxSnapshotDto;
import com.berijalan.ewallet.entity.Merchant;
import com.berijalan.ewallet.entity.MerchantTax;
import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.entity.constant.TaxValueType;
import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.repository.MerchantRepository;
import com.berijalan.ewallet.repository.MerchantTaxRepository;
import com.berijalan.ewallet.repository.TransactionRepository;
import com.berijalan.ewallet.repository.UserRepository;
import com.berijalan.ewallet.repository.WalletRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantTaxRepository merchantTaxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ResPaymentDto pay(ReqPayDto request, Long userId) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> {
                    log.warn("Payment rejected because wallet was not found. userId={}", userId);
                    return new BadRequestException("Wallet not found");
                });
        User user = wallet.getUser();

        Merchant merchant = merchantRepository.findByName(request.merchantName())
                .orElseThrow(() -> {
                    log.warn("Payment rejected because merchant was not found. merchantName={}, userId={}",
                            request.merchantName(), userId);
                    return new BadRequestException("Merchant not found");
                });

        long baseAmount = request.amount();
        List<TaxSnapshotDto> taxSnapshots = new ArrayList<>();

        List<MerchantTax> activeTaxes = merchantTaxRepository.findByMerchantIdAndIsActiveTrue(merchant.getId());
        long totalTax = calculateTax(baseAmount, activeTaxes, taxSnapshots);
        long finalAmount = baseAmount + totalTax;

        if (wallet.getBalance() < finalAmount) {
            log.warn("Payment rejected because balance is insufficient. userId={}, merchant={}, baseAmount={}, tax={}, finalAmount={}, balance={}",
                    userId, merchant.getName(), baseAmount, totalTax, finalAmount, wallet.getBalance());
            throw new BadRequestException("Insufficient balance");
        }

        String taxSnapshotJson = null;
        if (!taxSnapshots.isEmpty()) {
            try {
                taxSnapshotJson = objectMapper.writeValueAsString(taxSnapshots);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize tax snapshot", e);
            }
        }

        String referenceId = generateUniqueReferenceId();
        long balanceBefore = wallet.getBalance();
        long balanceAfter = balanceBefore - finalAmount;
        wallet.setBalance(balanceAfter);

        Transaction transaction = new Transaction();
        transaction.setReferenceId(referenceId);
        transaction.setAmount(finalAmount);
        transaction.setBaseAmount(baseAmount);
        transaction.setTaxAmount(totalTax);
        transaction.setTaxSnapshot(taxSnapshotJson);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(request.description());
        transaction.setType(TransactionType.PAYMENT);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setMerchant(merchant);
        transaction.setUser(user);

        transactionRepository.saveAndFlush(transaction);

        log.info("Payment success. userId={}, merchant={}, baseAmount={}, tax={}, finalAmount={}, referenceId={}",
                userId, merchant.getName(), baseAmount, totalTax, finalAmount, referenceId);

        return new ResPaymentDto(
                transaction.getId().longValue(),
                transaction.getReferenceId(),
                transaction.getAmount(),
                transaction.getBaseAmount(),
                transaction.getTaxAmount(),
                transaction.getBalanceBefore(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                merchant.getName(),
                transaction.getType().name(),
                transaction.getStatus().name()
        );
    }

    private long calculateTax(long baseAmount, List<MerchantTax> taxes, List<TaxSnapshotDto> snapshots) {
        long totalTax = 0L;
        for (MerchantTax tax : taxes) {
            long calculatedTax = tax.getValueType() == TaxValueType.PERCENTAGE
                    ? BigDecimal.valueOf(baseAmount)
                        .multiply(tax.getTaxValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                        .setScale(0, RoundingMode.HALF_UP).longValue()
                    : tax.getTaxValue().setScale(0, RoundingMode.HALF_UP).longValue();

            totalTax += calculatedTax;
            snapshots.add(new TaxSnapshotDto(
                    tax.getTaxName(),
                    tax.getTaxType().name(),
                    tax.getValueType().name(),
                    tax.getTaxValue(),
                    calculatedTax
            ));
        }
        return totalTax;
    }

    @Transactional(readOnly = true)
    public ResTransactionHistoryDto getTransactions(Long userId, ReqTransactionHistoryDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Pageable pageable = request.toPageable(Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> transactionPage = transactionRepository.findByUserWithFilters(
                user, request.status(), request.type(), pageable);

        List<ResTransactionItemDto> items = transactionPage.getContent().stream()
                .map(tx -> new ResTransactionItemDto(
                        tx.getId().longValue(),
                        tx.getUser().getId().longValue(),
                        tx.getUser().getName(),
                        tx.getUser().getEmail(),
                        tx.getReferenceId(),
                        tx.getAmount(),
                        tx.getBaseAmount(),
                        tx.getTaxAmount(),
                        tx.getBalanceBefore(),
                        tx.getBalanceAfter(),
                        tx.getType().name(),
                        tx.getStatus().name(),
                        tx.getDescription(),
                        tx.getMerchant() != null ? tx.getMerchant().getName() : null,
                        tx.getCreatedAt()
                ))
                .toList();

        return new ResTransactionHistoryDto(
                items,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages()
        );
    }

    private String generateUniqueReferenceId() {
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
