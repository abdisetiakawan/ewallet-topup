package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.request.ReqTopupDto;
import com.berijalan.ewallet.dto.response.ResTopupDto;
import com.berijalan.ewallet.dto.response.ResWalletBalanceDto;
import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.repository.TransactionRepository;
import com.berijalan.ewallet.repository.UserRepository;
import com.berijalan.ewallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public ResWalletBalanceDto getBalance(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Wallet not found"));

        return new ResWalletBalanceDto(wallet.getBalance());
    }

    @Transactional
    public ResTopupDto topup(ReqTopupDto request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Wallet not found"));

        if (transactionRepository.existsByReferenceId(request.referenceId())) {
            throw new BadRequestException("Duplicate transaction reference ID");
        }

        // Increase balance
        wallet.setBalance(wallet.getBalance() + request.amount());
        walletRepository.save(wallet);

        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(request.amount());
        transaction.setType(TransactionType.TOPUP);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setReferenceId("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        transactionRepository.save(transaction);

        return new ResTopupDto(
                transaction.getId().longValue(),
                transaction.getReferenceId(),
                wallet.getBalance(),
                transaction.getType().name(),
                transaction.getStatus().name()
        );
    }
}
