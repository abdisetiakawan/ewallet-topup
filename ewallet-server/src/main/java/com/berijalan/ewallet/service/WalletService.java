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

    /**
     * Mengambil saldo wallet yang terlihat oleh customer.
     *
     * @param userId ID customer pemilik wallet.
     * @return saldo wallet beserta waktu update terakhir.
     * @throws NotFoundException jika customer belum memiliki wallet.
     */
    public ResWalletBalanceDto getBalance(Long userId) {
        WalletCacheService.WalletBalanceCache cached = walletCacheService.get(userId);
        if (cached != null) {
            return walletMapper.toBalanceDto(cached);
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));

        ResWalletBalanceDto result = walletMapper.toBalanceDto(wallet);
        walletCacheService.put(userId, wallet.getBalance(), wallet.getUpdatedAt());
        return result;
    }

    /**
     * Menambah saldo wallet dan membuat catatan transaksi top-up dalam satu transaksi database.
     *
     * @param request nominal top-up yang sudah melewati validasi request.
     * @param userId ID customer pemilik wallet.
     * @return detail transaksi top-up yang berhasil dibuat.
     * @throws BadRequestException jika nominal top-up melanggar batas transaksi.
     * @throws NotFoundException jika wallet customer tidak ditemukan.
     */
    @Transactional
    public ResTopupDto topup(ReqTopupDto request, Long userId) {
        validateTopupAmount(request.amount());

        // WHY: Saldo dikunci agar top-up dan pembayaran paralel tidak saling menimpa nilai balance.
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));
        User user = wallet.getUser();

        String referenceId = ReferenceIdGenerator.generate("TXN-");

        long balanceBefore = wallet.getBalance();
        long balanceAfter = safeAddBalance(balanceBefore, request.amount());

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
        // WHY: Cache baru aman diperbarui setelah transaksi commit agar nilai rollback tidak terbaca customer.
        walletCacheService.putAfterCommit(userId, balanceAfter, wallet.getUpdatedAt());

        return walletMapper.toTopupDto(transaction);
    }

    private void validateTopupAmount(Long amount) {
        if (amount == null) {
            throw new BadRequestException("Amount is required");
        }

        if (amount < TransactionAmountLimits.MIN_TRANSACTION_AMOUNT) {
            throw new BadRequestException("Minimum top-up amount is 10000");
        }

        if (amount > TransactionAmountLimits.MAX_TOPUP_AMOUNT) {
            throw new BadRequestException("Maximum top-up amount is 10000000");
        }
    }

    private long safeAddBalance(long balanceBefore, long amount) {
        try {
            return Math.addExact(balanceBefore, amount);
        } catch (ArithmeticException ex) {
            throw new BadRequestException("Wallet balance limit exceeded");
        }
    }

}
