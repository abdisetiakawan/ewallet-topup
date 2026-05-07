package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.request.ReqPayDto;
import com.berijalan.ewallet.dto.response.ResTransactionHistoryDto;
import com.berijalan.ewallet.dto.response.ResTransactionItemDto;
import com.berijalan.ewallet.entity.Merchant;
import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import com.berijalan.ewallet.repository.MerchantRepository;
import com.berijalan.ewallet.repository.TransactionRepository;
import com.berijalan.ewallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;

    @Transactional
    public void pay(ReqPayDto request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found: {}", email);
                    return new RuntimeException("User not found");
                });

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            log.error("Wallet not found for user: {}", email);
            throw new RuntimeException("Wallet not found");
        }

        if (wallet.getBalance() < request.amount()) {
            log.warn("Payment failed: Insufficient balance for user {}", email);
            throw new RuntimeException("Saldo tidak mencukupi");
        }

        Merchant merchant = merchantRepository.findByName(request.merchantName())
                .orElseThrow(() -> {
                    log.error("Merchant not found: {}", request.merchantName());
                    return new RuntimeException("Merchant tidak ditemukan");
                });

        wallet.setBalance(wallet.getBalance() - request.amount());

        Transaction transaction = new Transaction();
        transaction.setReferenceId("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setAmount(request.amount());
        transaction.setType(TransactionType.PAYMENT);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setMerchant(merchant);
        transaction.setUser(user);
        
        transactionRepository.save(transaction);

        log.info("Payment success. User: {}, Merchant: {}, Amount: {}", 
                 email, merchant.getName(), request.amount());
    }

    public ResTransactionHistoryDto getTransactions(String email, int page, int size, String status) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionPage;

        if (status != null && !status.isEmpty()) {
            TransactionStatus transactionStatus = TransactionStatus.valueOf(status.toUpperCase());
            transactionPage = transactionRepository.findByUserAndStatus(user, transactionStatus, pageable);
        } else {
            transactionPage = transactionRepository.findByUser(user, pageable);
        }

        List<ResTransactionItemDto> items = transactionPage.getContent().stream()
                .map(tx -> new ResTransactionItemDto(
                        tx.getId().longValue(),
                        tx.getReferenceId(),
                        tx.getAmount(),
                        tx.getType().name(),
                        tx.getStatus().name(),
                        tx.getMerchant() != null ? tx.getMerchant().getName() : null,
                        tx.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new ResTransactionHistoryDto(
                items,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages()
        );
    }
}