package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.request.ReqPayDto;
import com.berijalan.ewallet.dto.request.ReqTransactionHistoryDto;
import com.berijalan.ewallet.dto.response.ResPaymentDto;
import com.berijalan.ewallet.dto.response.ResTransactionHistoryDto;
import com.berijalan.ewallet.dto.response.ResTransactionItemDto;
import com.berijalan.ewallet.entity.Merchant;
import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.repository.MerchantRepository;
import com.berijalan.ewallet.repository.TransactionRepository;
import com.berijalan.ewallet.repository.UserRepository;
import com.berijalan.ewallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;

    @Transactional
    public ResPaymentDto pay(ReqPayDto request, String email) {
        String normalizedReferenceId = request.referenceId().trim();
        if (transactionRepository.existsByReferenceId(normalizedReferenceId)) {
            log.warn("Payment rejected because referenceId already exists. referenceId={}, user={}",
                    normalizedReferenceId, email);
            throw new BadRequestException("Reference ID already used");
        }

        Wallet wallet = walletRepository.findByUserEmailForUpdate(email)
                .orElseThrow(() -> {
                    log.warn("Payment rejected because wallet was not found. user={}", email);
                    return new BadRequestException("Wallet not found");
                });
        User user = wallet.getUser();

        Merchant merchant = merchantRepository.findByName(request.merchantName())
                .orElseThrow(() -> {
                    log.warn("Payment rejected because merchant was not found. merchantName={}, user={}",
                            request.merchantName(), email);
                    return new BadRequestException("Merchant not found");
                });

        if (wallet.getBalance() < request.amount()) {
            log.warn("Payment rejected because balance is insufficient. user={}, merchant={}, amount={}, balance={}",
                    email, merchant.getName(), request.amount(), wallet.getBalance());
            throw new BadRequestException("Insufficient balance");
        }

        long balanceBefore = wallet.getBalance();
        long balanceAfter = balanceBefore - request.amount();
        wallet.setBalance(balanceAfter);

        Transaction transaction = new Transaction();
        transaction.setReferenceId(normalizedReferenceId);
        transaction.setAmount(request.amount());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(request.description());
        transaction.setType(TransactionType.PAYMENT);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setMerchant(merchant);
        transaction.setUser(user);

        Transaction savedTransaction;
        try {
            savedTransaction = transactionRepository.saveAndFlush(transaction);
        } catch (DataIntegrityViolationException e) {
            log.warn("Payment rejected because transaction data violates a database constraint. referenceId={}, user={}",
                    normalizedReferenceId, email);
            throw new BadRequestException("Reference ID already used");
        }

        log.info("Payment success. user={}, merchant={}, amount={}, referenceId={}",
                email, merchant.getName(), request.amount(), savedTransaction.getReferenceId(), balanceAfter);

        return new ResPaymentDto(
                savedTransaction.getId().longValue(),
                savedTransaction.getReferenceId(),
                savedTransaction.getAmount(),
                savedTransaction.getBalanceBefore(),
                savedTransaction.getBalanceAfter(),
                savedTransaction.getDescription(),
                merchant.getName(),
                savedTransaction.getType().name(),
                savedTransaction.getStatus().name()
        );
    }

    @Transactional(readOnly = true)
    public ResTransactionHistoryDto getTransactions(String email, ReqTransactionHistoryDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Pageable pageable = request.toPageable(Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> transactionPage;
        TransactionStatus status = request.status();
        TransactionType type = request.type();

        if (status != null && type != null) {
            transactionPage = transactionRepository.findByUserAndStatusAndType(user, status, type, pageable);
        } else if (status != null) {
            transactionPage = transactionRepository.findByUserAndStatus(user, status, pageable);
        } else if (type != null) {
            transactionPage = transactionRepository.findByUserAndType(user, type, pageable);
        } else {
            transactionPage = transactionRepository.findByUser(user, pageable);
        }

        List<ResTransactionItemDto> items = transactionPage.getContent().stream()
                .map(tx -> new ResTransactionItemDto(
                        tx.getId().longValue(),
                        tx.getUser().getId().longValue(),
                        tx.getUser().getName(),
                        tx.getUser().getEmail(),
                        tx.getReferenceId(),
                        tx.getAmount(),
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


}
