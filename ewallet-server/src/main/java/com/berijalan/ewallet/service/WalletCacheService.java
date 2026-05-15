package com.berijalan.ewallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletCacheService {

    private static final String KEY_PREFIX = "wallet:balance";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.wallet.ttl-minutes:30}")
    private long ttlMinutes;

    public WalletBalanceCache get(Long userId) {
        try {
            Object raw = redisTemplate.opsForValue().get(buildKey(userId));
            if (raw == null) {
                log.debug("Cache miss: wallet balance for userId={}", userId);
                return null;
            }
            log.debug("Cache hit: wallet balance for userId={}", userId);
            return objectMapper.convertValue(raw, WalletBalanceCache.class);
        } catch (Exception e) {
            log.warn("Failed to read wallet cache for userId={}, falling back to DB. Cause: {}", userId, e.getMessage());
            return null;
        }
    }

    public void put(Long userId, Long balance, LocalDateTime updatedAt) {
        try {
            redisTemplate.opsForValue().set(
                    buildKey(userId),
                    new WalletBalanceCache(balance, updatedAt),
                    Duration.ofMinutes(ttlMinutes)
            );
            log.debug("Cached wallet balance for userId={}, balance={}", userId, balance);
        } catch (Exception e) {
            log.warn("Failed to write wallet cache for userId={}. Cause: {}", userId, e.getMessage());
        }
    }

    /**
     * Schedules a cache update to fire after the current DB transaction commits.
     * If no transaction is active, writes to cache immediately.
     * Prevents caching a value that may be rolled back.
     */
    public void putAfterCommit(Long userId, Long balance, LocalDateTime updatedAt) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    put(userId, balance, updatedAt);
                    log.debug("Write-through cache update (post-commit) for userId={}", userId);
                }
            });
        } else {
            put(userId, balance, updatedAt);
        }
    }

    private String buildKey(Long userId) {
        return KEY_PREFIX + ":" + userId;
    }

    public record WalletBalanceCache(
            Long balance,
            LocalDateTime updatedAt
    ) {
    }
}
