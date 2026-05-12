package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.request.ReqTopupDto;
import com.berijalan.ewallet.dto.response.ResTopupDto;
import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.repository.TransactionRepository;
import com.berijalan.ewallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void topup_whenWalletExists_shouldIncreaseBalanceAndCreateTransaction() {
        Long userId = 1L;
        Wallet wallet = createWallet(userId, 100_000L);
        ReqTopupDto request = new ReqTopupDto(50_000L);

        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        when(transactionRepository.saveAndFlush(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction transaction = invocation.getArgument(0);
                    transaction.setId(10L);
                    return transaction;
                });

        ResTopupDto response = walletService.topup(request, userId);

        assertThat(wallet.getBalance()).isEqualTo(150_000L);
        assertThat(response.amount()).isEqualTo(50_000L);
        assertThat(response.balanceBefore()).isEqualTo(100_000L);
        assertThat(response.balanceAfter()).isEqualTo(150_000L);
        assertThat(response.type()).isEqualTo(TransactionType.TOPUP.name());
        assertThat(response.status()).isEqualTo(TransactionStatus.SUCCESS.name());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).saveAndFlush(captor.capture());

        Transaction savedTransaction = captor.getValue();

        assertThat(savedTransaction.getUser()).isSameAs(wallet.getUser());
        assertThat(savedTransaction.getAmount()).isEqualTo(50_000L);
        assertThat(savedTransaction.getBaseAmount()).isEqualTo(50_000L);
        assertThat(savedTransaction.getTaxAmount()).isZero();
        assertThat(savedTransaction.getBalanceBefore()).isEqualTo(100_000L);
        assertThat(savedTransaction.getBalanceAfter()).isEqualTo(150_000L);
        assertThat(savedTransaction.getType()).isEqualTo(TransactionType.TOPUP);
        assertThat(savedTransaction.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(savedTransaction.getReferenceId()).startsWith("TXN-");
    }

    @Test
    void topup_whenWalletDoesNotExist_shouldThrowBadRequestException() {
        Long userId = 99L;
        ReqTopupDto request = new ReqTopupDto(50_000L);

        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.topup(request, userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Wallet not found");

        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never()).saveAndFlush(any(Transaction.class));
    }

    private Wallet createWallet(Long userId, Long balance) {
        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");

        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);
        wallet.setBalance(balance);

        user.setWallet(wallet);

        return wallet;
    }
}
