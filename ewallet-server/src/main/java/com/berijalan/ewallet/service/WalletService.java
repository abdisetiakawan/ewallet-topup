package com.berijalan.ewallet.service;

import com.berijalan.ewallet.common.TransactionAmountLimits;
import com.berijalan.ewallet.dto.request.ReqTopupDto;
import com.berijalan.ewallet.dto.response.ResTopupDto;
import com.berijalan.ewallet.dto.response.ResWalletBalanceDto;
import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.exception.NotFoundException;
import com.berijalan.ewallet.mapper.WalletMapper;
import com.berijalan.ewallet.repository.TransactionRepository;
import com.berijalan.ewallet.repository.WalletRepository;
import com.berijalan.ewallet.util.ReferenceIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WalletCacheService walletCacheService;
    private final WalletMapper walletMapper;

    public ResWalletBalanceDto getBalance(Long userId) {
        log.debug("Wallet balance requested. userId={}", userId);

        WalletCacheService.WalletBalanceCache cached = walletCacheService.get(userId);
        if (cached != null) {
            log.debug("Wallet balance served from cache. userId={}, balance={}", userId, cached.balance());
            return walletMapper.toBalanceDto(cached);
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Wallet balance request rejected because wallet was not found. userId={}", userId);
                    return new NotFoundException("Wallet not found");
                });

        ResWalletBalanceDto result = walletMapper.toBalanceDto(wallet);
        walletCacheService.put(userId, wallet.getBalance(), wallet.getUpdatedAt());
        log.debug("Wallet balance served from database. userId={}, balance={}", userId, wallet.getBalance());
        return result;
    }

    @Transactional
    public ResTopupDto topup(ReqTopupDto request, Long userId) {
        log.info("Top-up requested. userId={}, amount={}", userId, request.amount());

        validateTopupAmount(request.amount(), userId);

        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> {
                    log.warn("Top-up rejected because wallet was not found. userId={}", userId);
                    return new NotFoundException("Wallet not found");
                });
        User user = wallet.getUser();

        String referenceId = ReferenceIdGenerator.generate("TXN-");

        long balanceBefore = wallet.getBalance();
        long balanceAfter = safeAddBalance(balanceBefore, request.amount(), userId);

        wallet.setBalance(balanceAfter);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(request.amount());
        transaction.setBaseAmount(request.amount());
        transaction.setTaxAmount(0L);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setType(TransactionType.TOPUP);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setReferenceId(referenceId);

        transaction = transactionRepository.saveAndFlush(transaction);
        walletCacheService.putAfterCommit(userId, balanceAfter, wallet.getUpdatedAt());

        log.info("Top-up success. userId={}, amount={}, balanceBefore={}, balanceAfter={}, referenceId={}",
                userId, request.amount(), balanceBefore, balanceAfter, transaction.getReferenceId());

        return walletMapper.toTopupDto(transaction);
    }

    private void validateTopupAmount(Long amount, Long userId) {
        if (amount == null) {
            log.warn("Top-up rejected because amount is missing. userId={}", userId);
            throw new BadRequestException("Amount is required");
        }

        if (amount < TransactionAmountLimits.MIN_TRANSACTION_AMOUNT) {
            log.warn("Top-up rejected because amount is below minimum. userId={}, amount={}, minimum={}",
                    userId, amount, TransactionAmountLimits.MIN_TRANSACTION_AMOUNT);
            throw new BadRequestException("Minimum top-up amount is 10000");
        }

        if (amount > TransactionAmountLimits.MAX_TOPUP_AMOUNT) {
            log.warn("Top-up rejected because amount exceeds maximum. userId={}, amount={}, maximum={}",
                    userId, amount, TransactionAmountLimits.MAX_TOPUP_AMOUNT);
            throw new BadRequestException("Maximum top-up amount is 10000000");
        }
    }

    private long safeAddBalance(long balanceBefore, long amount, Long userId) {
        try {
            return Math.addExact(balanceBefore, amount);
        } catch (ArithmeticException ex) {
            log.warn("Top-up rejected because wallet balance overflowed. userId={}, balanceBefore={}, amount={}",
                    userId, balanceBefore, amount);
            throw new BadRequestException("Wallet balance limit exceeded");
        }
    }

}
