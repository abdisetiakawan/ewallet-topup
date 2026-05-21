package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.request.ReqPayDto;
import com.berijalan.ewallet.dto.request.ReqTransactionHistoryDto;
import com.berijalan.ewallet.dto.response.ResPaymentDto;
import com.berijalan.ewallet.dto.response.ResTransactionDetailDto;
import com.berijalan.ewallet.dto.response.ResTransactionHistoryDto;
import com.berijalan.ewallet.entity.Merchant;
import com.berijalan.ewallet.entity.MerchantTax;
import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.entity.constant.TaxType;
import com.berijalan.ewallet.entity.constant.TaxValueType;
import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.exception.NotFoundException;
import com.berijalan.ewallet.mapper.TransactionMapper;
import com.berijalan.ewallet.repository.MerchantRepository;
import com.berijalan.ewallet.repository.MerchantTaxRepository;
import com.berijalan.ewallet.repository.TransactionRepository;
import com.berijalan.ewallet.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private MerchantTaxRepository merchantTaxRepository;

    @Mock
    private WalletCacheService walletCacheService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Spy
    private TaxCalculator taxCalculator = new TaxCalculator();

    @Spy
    private TransactionMapper transactionMapper = new TransactionMapper();

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void pay_whenPaymentSucceeds_shouldDecreaseBalancePreciselyAndCreateTransaction() {
        Long userId = 1L;
        Wallet wallet = createWallet(userId, 200_000L);
        Merchant merchant = createMerchant(1L, "Gopay");
        ReqPayDto request = new ReqPayDto("Gopay", 100_000L, "Top-up Gopay");
        LocalDateTime persistedUpdatedAt = LocalDateTime.of(2026, 5, 15, 8, 45);

        List<MerchantTax> taxes = List.of(
                createTax(merchant, "Admin Fee", TaxType.ADMIN_FEE, TaxValueType.FIXED, "1000.0000"),
                createTax(merchant, "Service Fee", TaxType.SERVICE_FEE, TaxValueType.PERCENTAGE, "1.5000")
        );

        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
        when(merchantRepository.findByName("Gopay")).thenReturn(Optional.of(merchant));
        when(merchantTaxRepository.findByMerchantIdAndIsActiveTrue(merchant.getId())).thenReturn(taxes);
        when(transactionRepository.saveAndFlush(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(20L);
            ReflectionTestUtils.setField(wallet, "updatedAt", persistedUpdatedAt);
            return transaction;
        });

        ResPaymentDto response = transactionService.pay(request, userId);

        assertThat(wallet.getBalance()).isEqualTo(97_500L);

        assertThat(response.transactionId()).isEqualTo(20L);
        assertThat(response.amount()).isEqualTo(102_500L);
        assertThat(response.baseAmount()).isEqualTo(100_000L);
        assertThat(response.taxAmount()).isEqualTo(2_500L);
        assertThat(response.balanceBefore()).isEqualTo(200_000L);
        assertThat(response.balanceAfter()).isEqualTo(97_500L);
        assertThat(response.description()).isEqualTo("Top-up Gopay");
        assertThat(response.merchantName()).isEqualTo("Gopay");
        assertThat(response.type()).isEqualTo(TransactionType.PAYMENT.name());
        assertThat(response.status()).isEqualTo(TransactionStatus.SUCCESS.name());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).saveAndFlush(captor.capture());

        Transaction savedTransaction = captor.getValue();

        assertThat(savedTransaction.getUser()).isSameAs(wallet.getUser());
        assertThat(savedTransaction.getMerchant()).isSameAs(merchant);
        assertThat(savedTransaction.getAmount()).isEqualTo(102_500L);
        assertThat(savedTransaction.getBaseAmount()).isEqualTo(100_000L);
        assertThat(savedTransaction.getTaxAmount()).isEqualTo(2_500L);
        assertThat(savedTransaction.getBalanceBefore()).isEqualTo(200_000L);
        assertThat(savedTransaction.getBalanceAfter()).isEqualTo(97_500L);
        assertThat(savedTransaction.getDescription()).isEqualTo("Top-up Gopay");
        assertThat(savedTransaction.getType()).isEqualTo(TransactionType.PAYMENT);
        assertThat(savedTransaction.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(savedTransaction.getReferenceId()).startsWith("PAY-");
        assertThat(savedTransaction.getTaxSnapshot()).contains("Admin Fee", "Service Fee");

        InOrder inOrder = inOrder(transactionRepository, walletCacheService);
        inOrder.verify(transactionRepository).saveAndFlush(any(Transaction.class));
        inOrder.verify(walletCacheService).putAfterCommit(userId, 97_500L, persistedUpdatedAt);
    }

    @Test
    void pay_whenBalanceIsInsufficient_shouldThrowBadRequestException() {
        Long userId = 1L;
        Wallet wallet = createWallet(userId, 50_000L);
        Merchant merchant = createMerchant(1L, "Gopay");
        ReqPayDto request = new ReqPayDto("Gopay", 100_000L, "Top-up Gopay");

        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
        when(merchantRepository.findByName("Gopay")).thenReturn(Optional.of(merchant));
        when(merchantTaxRepository.findByMerchantIdAndIsActiveTrue(merchant.getId())).thenReturn(List.of());

        assertThatThrownBy(() -> transactionService.pay(request, userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Insufficient balance");

        assertThat(wallet.getBalance()).isEqualTo(50_000L);
        verify(transactionRepository, never()).saveAndFlush(any(Transaction.class));
    }

    @Test
    void pay_whenMerchantDoesNotExist_shouldThrowNotFoundException() {
        Long userId = 1L;
        Wallet wallet = createWallet(userId, 200_000L);
        ReqPayDto request = new ReqPayDto("Merchant Fiktif", 100_000L, "Top-up merchant fiktif");

        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
        when(merchantRepository.findByName("Merchant Fiktif")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.pay(request, userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Merchant not found");

        assertThat(wallet.getBalance()).isEqualTo(200_000L);
        verify(merchantTaxRepository, never()).findByMerchantIdAndIsActiveTrue(any());
        verify(transactionRepository, never()).saveAndFlush(any(Transaction.class));
    }

    @Test
    void pay_whenAmountExceedsLimit_shouldThrowBadRequestExceptionWithoutQueryingDatabase() {
        Long userId = 1L;
        ReqPayDto request = new ReqPayDto("Gopay", 10_000_001L, "Top-up Gopay");

        assertThatThrownBy(() -> transactionService.pay(request, userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Maximum payment amount is 10000000");

        verify(walletRepository, never()).findByUserIdForUpdate(any(Long.class));
        verify(transactionRepository, never()).saveAndFlush(any(Transaction.class));
    }

    @Test
    void getTransactions_whenRepositoryReturnsPage_shouldReturnTransactionHistory() {
        Long userId = 1L;
        User user = createUser(userId);
        Merchant merchant = createMerchant(1L, "Gopay");
        Transaction transaction = createPaymentTransaction(user, merchant);
        ReqTransactionHistoryDto request = new ReqTransactionHistoryDto(
                0,
                10,
                TransactionStatus.SUCCESS,
                TransactionType.PAYMENT
        );
        Pageable pageable = request.toPageable(Sort.by(Sort.Direction.DESC, "createdAt"));

        when(transactionRepository.findByUserIdAndStatusAndType(
                eq(userId),
                eq(TransactionStatus.SUCCESS),
                eq(TransactionType.PAYMENT),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(transaction), pageable, 1));

        ResTransactionHistoryDto response = transactionService.getTransactions(userId, request);

        assertThat(response.content()).hasSize(1);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);

        assertThat(response.content().get(0).transactionId()).isEqualTo(100L);
        assertThat(response.content().get(0).userId()).isEqualTo(userId);
        assertThat(response.content().get(0).userName()).isEqualTo("Test User");
        assertThat(response.content().get(0).userEmail()).isEqualTo("test@example.com");
        assertThat(response.content().get(0).referenceId()).isEqualTo("PAY-TEST");
        assertThat(response.content().get(0).amount()).isEqualTo(102_500L);
        assertThat(response.content().get(0).baseAmount()).isEqualTo(100_000L);
        assertThat(response.content().get(0).taxAmount()).isEqualTo(2_500L);
        assertThat(response.content().get(0).balanceBefore()).isEqualTo(200_000L);
        assertThat(response.content().get(0).balanceAfter()).isEqualTo(97_500L);
        assertThat(response.content().get(0).type()).isEqualTo(TransactionType.PAYMENT.name());
        assertThat(response.content().get(0).status()).isEqualTo(TransactionStatus.SUCCESS.name());
        assertThat(response.content().get(0).description()).isEqualTo("Top-up Gopay");
        assertThat(response.content().get(0).merchantName()).isEqualTo("Gopay");
    }

    @Test
    void getTransactions_whenNoTransactions_shouldReturnEmptyPage() {
        Long userId = 1L;
        ReqTransactionHistoryDto request = new ReqTransactionHistoryDto(
                0,
                10,
                null,
                null
        );
        Pageable pageable = request.toPageable(Sort.by(Sort.Direction.DESC, "createdAt"));

        when(transactionRepository.findByUserId(
                eq(userId),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        ResTransactionHistoryDto response = transactionService.getTransactions(userId, request);

        assertThat(response.content()).isEmpty();
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
    }

    @Test
    void getTransaction_whenOwnedPaymentExists_shouldReturnDetailAndTaxSnapshots() {
        Long userId = 1L;
        User user = createUser(userId);
        Transaction transaction = createPaymentTransaction(user, createMerchant(1L, "Gopay"));
        transaction.setTaxSnapshot("""
                [{
                  "taxName": "Service Fee",
                  "taxType": "SERVICE_FEE",
                  "valueType": "PERCENTAGE",
                  "taxValue": 1.5000,
                  "calculatedTax": 1500
                }]
                """);

        when(transactionRepository.findByIdAndUserId(100L, userId)).thenReturn(Optional.of(transaction));

        ResTransactionDetailDto response = transactionService.getTransaction(userId, 100L);

        assertThat(response.transactionId()).isEqualTo(100L);
        assertThat(response.referenceId()).isEqualTo("PAY-TEST");
        assertThat(response.merchantName()).isEqualTo("Gopay");
        assertThat(response.taxDetails()).hasSize(1);
        assertThat(response.taxDetails().get(0).taxName()).isEqualTo("Service Fee");
        assertThat(response.taxDetails().get(0).taxCategory()).isEqualTo("SERVICE_FEE");
        assertThat(response.taxDetails().get(0).valueType()).isEqualTo("PERCENTAGE");
        assertThat(response.taxDetails().get(0).calculatedAmount()).isEqualTo(1_500L);
    }

    @Test
    void getTransaction_whenOwnedTopupHasNoTaxSnapshot_shouldReturnEmptyTaxDetails() {
        Long userId = 1L;
        Transaction transaction = createPaymentTransaction(createUser(userId), createMerchant(1L, "Gopay"));
        transaction.setType(TransactionType.TOPUP);
        transaction.setMerchant(null);
        transaction.setTaxAmount(0L);

        when(transactionRepository.findByIdAndUserId(100L, userId)).thenReturn(Optional.of(transaction));

        ResTransactionDetailDto response = transactionService.getTransaction(userId, 100L);

        assertThat(response.merchantName()).isNull();
        assertThat(response.taxDetails()).isEmpty();
    }

    @Test
    void getTransaction_whenTransactionIsMissingOrNotOwned_shouldThrowNotFoundException() {
        when(transactionRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransaction(1L, 100L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Transaction not found");
    }

    private User createUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        return user;
    }

    private Wallet createWallet(Long userId, Long balance) {
        User user = createUser(userId);

        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);
        wallet.setBalance(balance);

        user.setWallet(wallet);

        return wallet;
    }

    private Merchant createMerchant(Long id, String name) {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setName(name);
        merchant.setIsActive(true);
        return merchant;
    }

    private MerchantTax createTax(
            Merchant merchant,
            String taxName,
            TaxType taxType,
            TaxValueType valueType,
            String taxValue
    ) {
        MerchantTax tax = new MerchantTax();
        tax.setId(1L);
        tax.setMerchant(merchant);
        tax.setTaxName(taxName);
        tax.setTaxType(taxType);
        tax.setValueType(valueType);
        tax.setTaxValue(new BigDecimal(taxValue));
        tax.setIsActive(true);
        return tax;
    }

    private Transaction createPaymentTransaction(User user, Merchant merchant) {
        Transaction transaction = new Transaction();
        transaction.setId(100L);
        transaction.setUser(user);
        transaction.setMerchant(merchant);
        transaction.setReferenceId("PAY-TEST");
        transaction.setAmount(102_500L);
        transaction.setBaseAmount(100_000L);
        transaction.setTaxAmount(2_500L);
        transaction.setBalanceBefore(200_000L);
        transaction.setBalanceAfter(97_500L);
        transaction.setDescription("Top-up Gopay");
        transaction.setType(TransactionType.PAYMENT);
        transaction.setStatus(TransactionStatus.SUCCESS);
        return transaction;
    }
}
