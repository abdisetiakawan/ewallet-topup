package com.berijalan.ewallet.mapper;

import com.berijalan.ewallet.dto.response.ResTopupDto;
import com.berijalan.ewallet.dto.response.ResWalletBalanceDto;
import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.service.WalletCacheService.WalletBalanceCache;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper implements BaseMapper<Wallet, ResWalletBalanceDto> {

    @Override
    public ResWalletBalanceDto toDto(Wallet wallet) {
        return new ResWalletBalanceDto(wallet.getBalance(), wallet.getUpdatedAt());
    }

    public ResWalletBalanceDto toBalanceDto(Wallet wallet) {
        return toDto(wallet);
    }

    public ResWalletBalanceDto toBalanceDto(WalletBalanceCache cache) {
        return new ResWalletBalanceDto(cache.balance(), cache.updatedAt());
    }

    public ResTopupDto toTopupDto(Transaction transaction) {
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
}
