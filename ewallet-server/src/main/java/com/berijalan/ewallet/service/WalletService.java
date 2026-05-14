package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.request.ReqTopupDto;
import com.berijalan.ewallet.dto.response.ResTopupDto;
import com.berijalan.ewallet.dto.response.ResWalletBalanceDto;
import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import com.berijalan.ewallet.exception.NotFoundException;
import com.berijalan.ewallet.repository.TransactionRepository;
import com.berijalan.ewallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WalletCacheService walletCacheService;

    public ResWalletBalanceDto getBalance(Long userId) {
        ResWalletBalanceDto cached = walletCacheService.get(userId);
        if (cached != null) return cached;

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));

        ResWalletBalanceDto result = new ResWalletBalanceDto(wallet.getBalance(), wallet.getUpdatedAt());
        walletCacheService.put(userId, wallet.getBalance(), wallet.getUpdatedAt());
        return result;
    }

    @Transactional
    public ResTopupDto topup(ReqTopupDto request, Long userId) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));
        User user = wallet.getUser();

        String referenceId = generateUniqueReferenceId();

        long balanceBefore = wallet.getBalance();
        long balanceAfter = balanceBefore + request.amount();

        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);
        walletCacheService.putAfterCommit(userId, balanceAfter, LocalDateTime.now());

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

        transactionRepository.saveAndFlush(transaction);

        return new ResTopupDto(
                transaction.getId().longValue(),
                transaction.getAmount(),
                transaction.getBalanceBefore(),
                transaction.getBalanceAfter(),
                transaction.getType().name(),
                transaction.getStatus().name(),
                transaction.getCreatedAt()
        );
    }

    private String generateUniqueReferenceId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
