package com.berijalan.ewallet.mapper;

import com.berijalan.ewallet.dto.response.ResAdminMerchantDto;
import com.berijalan.ewallet.dto.response.ResLoginDto;
import com.berijalan.ewallet.dto.response.ResMerchantDto;
import com.berijalan.ewallet.dto.response.ResPaymentDto;
import com.berijalan.ewallet.dto.response.ResTopupDto;
import com.berijalan.ewallet.dto.response.ResTransactionHistoryDto;
import com.berijalan.ewallet.dto.response.ResUserSummaryDto;
import com.berijalan.ewallet.dto.response.ResWalletBalanceDto;
import com.berijalan.ewallet.entity.Merchant;
import com.berijalan.ewallet.entity.MerchantTax;
import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.entity.constant.RoleName;
import com.berijalan.ewallet.entity.constant.TaxType;
import com.berijalan.ewallet.entity.constant.TaxValueType;
import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import com.berijalan.ewallet.security.UserDetailsImpl;
import com.berijalan.ewallet.service.WalletCacheService.WalletBalanceCache;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MapperLayerTest {

    private final WalletMapper walletMapper = new WalletMapper();
    private final TransactionMapper transactionMapper = new TransactionMapper();
    private final MerchantMapper merchantMapper = new MerchantMapper();
    private final AdminMerchantMapper adminMerchantMapper = new AdminMerchantMapper();
    private final UserMapper userMapper = new UserMapper();

    @Test
    void walletMapper_shouldMapWalletAndCacheToBalanceResponse() {
        LocalDateTime updatedAt = LocalDateTime.of(2026, 5, 15, 8, 0);
        Wallet wallet = new Wallet();
        wallet.setBalance(125_000L);
        ReflectionTestUtils.setField(wallet, "updatedAt", updatedAt);

        ResWalletBalanceDto walletResponse = walletMapper.toBalanceDto(wallet);
        ResWalletBalanceDto cacheResponse = walletMapper.toBalanceDto(new WalletBalanceCache(150_000L, updatedAt));

        assertThat(walletResponse.balance()).isEqualTo(125_000L);
        assertThat(walletResponse.updatedAt()).isEqualTo(updatedAt);
        assertThat(cacheResponse.balance()).isEqualTo(150_000L);
        assertThat(cacheResponse.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void walletMapper_shouldMapTopupTransaction() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 15, 8, 15);
        Transaction transaction = new Transaction();
        transaction.setId(10L);
        transaction.setAmount(50_000L);
        transaction.setBalanceBefore(100_000L);
        transaction.setBalanceAfter(150_000L);
        transaction.setType(TransactionType.TOPUP);
        transaction.setStatus(TransactionStatus.SUCCESS);
        ReflectionTestUtils.setField(transaction, "createdAt", createdAt);

        ResTopupDto response = walletMapper.toTopupDto(transaction);

        assertThat(response.transactionId()).isEqualTo(10L);
        assertThat(response.amount()).isEqualTo(50_000L);
        assertThat(response.balanceBefore()).isEqualTo(100_000L);
        assertThat(response.balanceAfter()).isEqualTo(150_000L);
        assertThat(response.type()).isEqualTo("TOPUP");
        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void transactionMapper_shouldMapPaymentAndHistoryResponses() {
        User user = createUser(1L);
        Merchant merchant = createMerchant(2L, "Gopay", true);
        Transaction transaction = createPaymentTransaction(user, merchant);

        ResPaymentDto payment = transactionMapper.toPaymentDto(transaction);
        ResTransactionHistoryDto history = transactionMapper.toHistoryDto(
                new PageImpl<>(List.of(transaction), PageRequest.of(0, 10), 1)
        );

        assertThat(payment.transactionId()).isEqualTo(100L);
        assertThat(payment.referenceId()).isEqualTo("PAY-TEST");
        assertThat(payment.amount()).isEqualTo(102_500L);
        assertThat(payment.baseAmount()).isEqualTo(100_000L);
        assertThat(payment.taxAmount()).isEqualTo(2_500L);
        assertThat(payment.balanceBefore()).isEqualTo(200_000L);
        assertThat(payment.balanceAfter()).isEqualTo(97_500L);
        assertThat(payment.merchantName()).isEqualTo("Gopay");

        assertThat(history.content()).hasSize(1);
        assertThat(history.page()).isZero();
        assertThat(history.size()).isEqualTo(10);
        assertThat(history.totalElements()).isEqualTo(1);
        assertThat(history.content().get(0).userId()).isEqualTo(1L);
        assertThat(history.content().get(0).merchantName()).isEqualTo("Gopay");
    }

    @Test
    void merchantMapper_shouldMapOnlyActiveTaxes() {
        Merchant merchant = createMerchant(1L, "Gopay", true);
        merchant.getTaxes().add(createTax(merchant, 11L, "Admin Fee", TaxType.ADMIN_FEE, TaxValueType.FIXED, "1000.0000", true));
        merchant.getTaxes().add(createTax(merchant, 12L, "Inactive Fee", TaxType.SERVICE_FEE, TaxValueType.FIXED, "500.0000", false));

        ResMerchantDto response = merchantMapper.toDto(merchant);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Gopay");
        assertThat(response.isActive()).isTrue();
        assertThat(response.taxes()).hasSize(1);
        assertThat(response.taxes().get(0).taxName()).isEqualTo("Admin Fee");
    }

    @Test
    void adminMerchantMapper_shouldMapMerchantWithTaxes() {
        Merchant merchant = createMerchant(1L, "Gopay", true);
        List<MerchantTax> taxes = List.of(
                createTax(merchant, 11L, "Admin Fee", TaxType.ADMIN_FEE, TaxValueType.FIXED, "1000.0000", true),
                createTax(merchant, 12L, "Service Fee", TaxType.SERVICE_FEE, TaxValueType.PERCENTAGE, "1.5000", false)
        );

        ResAdminMerchantDto response = adminMerchantMapper.toDto(merchant, taxes);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Gopay");
        assertThat(response.taxes()).hasSize(2);
        assertThat(response.taxes().get(0).id()).isEqualTo(11L);
        assertThat(response.taxes().get(0).taxType()).isEqualTo("ADMIN_FEE");
        assertThat(response.taxes().get(0).valueType()).isEqualTo("FIXED");
    }

    @Test
    void userMapper_shouldMapUserAndLoginResponse() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 15, 8, 30);
        User user = createUser(1L);
        ReflectionTestUtils.setField(user, "createdAt", createdAt);

        ResUserSummaryDto summary = userMapper.toSummaryDto(user);
        ResLoginDto login = userMapper.toLoginDto(
                "access-token",
                3_600L,
                new UserDetailsImpl(1L, "Test User", "test@example.com", RoleName.CUSTOMER, createdAt, "password")
        );

        assertThat(summary.userId()).isEqualTo(1L);
        assertThat(summary.email()).isEqualTo("test@example.com");
        assertThat(summary.createdAt()).isEqualTo(createdAt);
        assertThat(summary.role()).isEqualTo("CUSTOMER");
        assertThat(login.token()).isEqualTo("access-token");
        assertThat(login.tokenType()).isEqualTo("Bearer");
        assertThat(login.expiresIn()).isEqualTo(3_600L);
        assertThat(login.user().userId()).isEqualTo(1L);
    }

    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(RoleName.CUSTOMER);
        return user;
    }

    private Merchant createMerchant(Long id, String name, Boolean isActive) {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setName(name);
        merchant.setIsActive(isActive);
        return merchant;
    }

    private MerchantTax createTax(
            Merchant merchant,
            Long id,
            String taxName,
            TaxType taxType,
            TaxValueType valueType,
            String taxValue,
            boolean isActive
    ) {
        MerchantTax tax = new MerchantTax();
        tax.setId(id);
        tax.setMerchant(merchant);
        tax.setTaxName(taxName);
        tax.setTaxType(taxType);
        tax.setValueType(valueType);
        tax.setTaxValue(new BigDecimal(taxValue));
        tax.setIsActive(isActive);
        tax.setEffectiveAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return tax;
    }

    private Transaction createPaymentTransaction(User user, Merchant merchant) {
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 15, 8, 45);
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
        ReflectionTestUtils.setField(transaction, "createdAt", createdAt);
        return transaction;
    }
}
