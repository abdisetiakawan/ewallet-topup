package com.berijalan.ewallet.integration;

import com.berijalan.ewallet.dto.request.ReqTopupDto;
import com.berijalan.ewallet.dto.request.ReqPayDto;
import com.berijalan.ewallet.entity.Merchant;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.repository.MerchantRepository;
import com.berijalan.ewallet.repository.UserRepository;
import com.berijalan.ewallet.repository.WalletRepository;
import com.berijalan.ewallet.service.TransactionService;
import com.berijalan.ewallet.service.WalletCacheService;
import com.berijalan.ewallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.data.redis.repositories.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate.SQL=OFF"
})
class WalletConcurrencyIntegrationTest {

    private static final long CONCURRENCY_TIMEOUT_SECONDS = 30L;

    @MockitoBean(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private WalletCacheService walletCacheService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    void concurrentTopup_shouldKeepFinalBalanceConsistent() throws Exception {
        User user = createUser();
        Wallet wallet = createWallet(user, 100_000L);

        int threadCount = 10;
        long topupAmount = 10_000L;
        long expectedFinalBalance = 100_000L + (threadCount * topupAmount);

        runConcurrently(threadCount, () -> walletService.topup(
                new ReqTopupDto(topupAmount),
                user.getId()
        ));

        Wallet updatedWallet = walletRepository.findById(wallet.getId())
                .orElseThrow();

        assertThat(updatedWallet.getBalance()).isEqualTo(expectedFinalBalance);
    }

    @Test
    void concurrentPayment_shouldKeepFinalBalanceConsistent() throws Exception {
        User user = createUser();
        Wallet wallet = createWallet(user, 100_000L);
        Merchant merchant = createMerchant("Concurrent Merchant " + UUID.randomUUID());

        int threadCount = 10;
        long paymentAmount = 10_000L;
        long expectedFinalBalance = 100_000L - (threadCount * paymentAmount);

        runConcurrently(threadCount, () -> transactionService.pay(
                new ReqPayDto(
                        merchant.getName(),
                        paymentAmount,
                        "Concurrent payment test"
                ),
                user.getId()
        ));

        Wallet updatedWallet = walletRepository.findById(wallet.getId())
                .orElseThrow();

        assertThat(updatedWallet.getBalance()).isEqualTo(expectedFinalBalance);
    }

    private void runConcurrently(int threadCount, ConcurrentTask task) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threadCount; i++) {
                futures.add(executorService.submit(() -> {
                    try {
                        readyLatch.countDown();
                        if (!startLatch.await(CONCURRENCY_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("Timed out waiting for start signal");
                        }

                        task.run();
                        return null;
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(exception);
                    }
                }));
            }

            if (!readyLatch.await(CONCURRENCY_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for worker threads");
            }
            startLatch.countDown();

            for (Future<?> future : futures) {
                future.get(CONCURRENCY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
        } finally {
            startLatch.countDown();
            executorService.shutdown();
            if (!executorService.awaitTermination(CONCURRENCY_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }
    }

    private User createUser() {
        User user = new User();
        user.setName("Concurrency User");
        user.setEmail("concurrency-" + UUID.randomUUID() + "@example.com");
        user.setPassword("password");

        return userRepository.saveAndFlush(user);
    }

    private Wallet createWallet(User user, Long balance) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(balance);

        return walletRepository.saveAndFlush(wallet);
    }

    private Merchant createMerchant(String name) {
        Merchant merchant = new Merchant();
        merchant.setName(name);
        merchant.setIsActive(true);

        return merchantRepository.saveAndFlush(merchant);
    }

    @FunctionalInterface
    private interface ConcurrentTask {
        void run() throws Exception;
    }
}
