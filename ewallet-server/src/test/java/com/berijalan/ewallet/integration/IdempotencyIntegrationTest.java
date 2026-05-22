package com.berijalan.ewallet.integration;

import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.entity.constant.RoleName;
import com.berijalan.ewallet.repository.TransactionRepository;
import com.berijalan.ewallet.repository.UserRepository;
import com.berijalan.ewallet.repository.WalletRepository;
import com.berijalan.ewallet.security.JwtUtils;
import com.berijalan.ewallet.service.WalletCacheService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.data.redis.repositories.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate.SQL=OFF"
})
@AutoConfigureMockMvc
class IdempotencyIntegrationTest {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String TOPUP_PATH = "/api/wallet/topup";

    private final Map<String, String> redisStore = new ConcurrentHashMap<>();
    private final List<Long> createdUserIds = new ArrayList<>();

    @MockitoBean(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @MockitoBean
    private WalletCacheService walletCacheService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUpRedisStore() {
        redisStore.clear();

        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenAnswer(invocation -> redisStore.putIfAbsent(
                        invocation.getArgument(0),
                        invocation.getArgument(1)
                ) == null);
        when(valueOperations.get(anyString()))
                .thenAnswer(invocation -> redisStore.get(invocation.getArgument(0)));
        doAnswer(invocation -> {
            redisStore.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(valueOperations).set(anyString(), anyString(), any(Duration.class));
        when(stringRedisTemplate.delete(anyString()))
                .thenAnswer(invocation -> redisStore.remove(invocation.getArgument(0)) != null);
    }

    @AfterEach
    void cleanUpCreatedData() {
        for (Long userId : createdUserIds) {
            transactionRepository.findByUserId(userId, PageRequest.of(0, 100))
                    .forEach(transactionRepository::delete);
            walletRepository.findByUserId(userId)
                    .ifPresent(walletRepository::delete);
            userRepository.deleteById(userId);
        }
    }

    @Test
    void repeatedTopupWithSameIdempotencyKey_shouldReplayResponseWithoutDuplicateMutation() throws Exception {
        TestCustomer customer = createCustomerWithWallet(100_000L);
        String token = jwtUtils.generateTokenForUserId(customer.user().getId());
        String idempotencyKey = "topup-repeat-" + UUID.randomUUID();

        MvcResult firstResult = performTopup(token, idempotencyKey, 50_000L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.amount").value(50_000))
                .andReturn();

        MvcResult secondResult = performTopup(token, idempotencyKey, 50_000L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.amount").value(50_000))
                .andReturn();

        MvcResult thirdResult = performTopup(token, idempotencyKey, 50_000L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.amount").value(50_000))
                .andReturn();

        String firstBody = firstResult.getResponse().getContentAsString();
        assertThat(secondResult.getResponse().getContentAsString()).isEqualTo(firstBody);
        assertThat(thirdResult.getResponse().getContentAsString()).isEqualTo(firstBody);

        JsonNode responseJson = objectMapper.readTree(firstBody);
        Long transactionId = responseJson.at("/data/transactionId").asLong();

        Wallet updatedWallet = walletRepository.findById(customer.wallet().getId()).orElseThrow();
        List<Transaction> transactions = transactionRepository
                .findByUserId(customer.user().getId(), PageRequest.of(0, 10))
                .getContent();

        assertThat(updatedWallet.getBalance()).isEqualTo(150_000L);
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getId()).isEqualTo(transactionId);
        assertThat(transactions.get(0).getAmount()).isEqualTo(50_000L);
        verify(walletCacheService, times(1))
                .putAfterCommit(eq(customer.user().getId()), eq(150_000L), any(LocalDateTime.class));
    }

    @Test
    void repeatedTopupWithSameIdempotencyKeyAndDifferentBody_shouldReturnConflict() throws Exception {
        TestCustomer customer = createCustomerWithWallet(100_000L);
        String token = jwtUtils.generateTokenForUserId(customer.user().getId());
        String idempotencyKey = "topup-conflict-" + UUID.randomUUID();

        performTopup(token, idempotencyKey, 50_000L)
                .andExpect(status().isOk());

        performTopup(token, idempotencyKey, 60_000L)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("Idempotency-Key already used for different request"));

        Wallet updatedWallet = walletRepository.findById(customer.wallet().getId()).orElseThrow();
        long transactionCount = transactionRepository
                .findByUserId(customer.user().getId(), PageRequest.of(0, 10))
                .getTotalElements();

        assertThat(updatedWallet.getBalance()).isEqualTo(150_000L);
        assertThat(transactionCount).isEqualTo(1L);
    }

    @Test
    void topupWithoutIdempotencyKey_shouldBeRejectedBeforeMutation() throws Exception {
        TestCustomer customer = createCustomerWithWallet(100_000L);
        String token = jwtUtils.generateTokenForUserId(customer.user().getId());

        mockMvc.perform(post(TOPUP_PATH)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":50000}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("Idempotency-Key header is required"));

        Wallet updatedWallet = walletRepository.findById(customer.wallet().getId()).orElseThrow();
        long transactionCount = transactionRepository
                .findByUserId(customer.user().getId(), PageRequest.of(0, 10))
                .getTotalElements();

        assertThat(updatedWallet.getBalance()).isEqualTo(100_000L);
        assertThat(transactionCount).isZero();
    }

    private org.springframework.test.web.servlet.ResultActions performTopup(
            String token,
            String idempotencyKey,
            Long amount
    ) throws Exception {
        return mockMvc.perform(post(TOPUP_PATH)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .header(IDEMPOTENCY_KEY_HEADER, idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"amount":%d}
                        """.formatted(amount)));
    }

    private TestCustomer createCustomerWithWallet(Long balance) {
        User user = new User();
        user.setName("Idempotency Test User");
        user.setEmail("idempotency-" + UUID.randomUUID() + "@example.com");
        user.setPassword("password");
        user.setRole(RoleName.CUSTOMER);
        user = userRepository.saveAndFlush(user);
        createdUserIds.add(user.getId());

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(balance);
        wallet = walletRepository.saveAndFlush(wallet);

        return new TestCustomer(user, wallet);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record TestCustomer(User user, Wallet wallet) {
    }
}
