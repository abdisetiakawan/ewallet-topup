package com.berijalan.ewallet.mapper;

import com.berijalan.ewallet.contract.model.ResTopupDto;
import com.berijalan.ewallet.contract.model.ResWalletBalanceDto;
import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.service.WalletCacheService.WalletBalanceCache;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper implements BaseMapper<Wallet, ResWalletBalanceDto> {

    @Override
    public ResWalletBalanceDto toDto(Wallet wallet) {
        return new ResWalletBalanceDto()
                .balance(wallet.getBalance())
                .updatedAt(wallet.getUpdatedAt());
    }

    public ResWalletBalanceDto toBalanceDto(Wallet wallet) {
        return toDto(wallet);
    }

    public ResWalletBalanceDto toBalanceDto(WalletBalanceCache cache) {
        return new ResWalletBalanceDto()
                .balance(cache.balance())
                .updatedAt(cache.updatedAt());
    }

    public ResTopupDto toTopupDto(Transaction transaction) {
        return new ResTopupDto()
                .transactionId(transaction.getId().longValue())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .type(transaction.getType().name())
                .status(transaction.getStatus().name())
                .createdAt(transaction.getCreatedAt());
    }
}
