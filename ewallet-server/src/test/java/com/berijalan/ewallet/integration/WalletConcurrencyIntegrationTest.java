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
import com.berijalan.ewallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.data.redis.repositories.enabled=false",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate.SQL=OFF"
})
class WalletConcurrencyIntegrationTest {

    @MockBean(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

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

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    walletService.topup(
                            new ReqTopupDto(topupAmount),
                            user.getId()
                    );
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executorService.shutdown();

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

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    transactionService.pay(
                            new ReqPayDto(
                                    merchant.getName(),
                                    paymentAmount,
                                    "Concurrent payment test"
                            ),
                            user.getId()
                    );
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executorService.shutdown();

        Wallet updatedWallet = walletRepository.findById(wallet.getId())
                .orElseThrow();

        assertThat(updatedWallet.getBalance()).isEqualTo(expectedFinalBalance);
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
}
