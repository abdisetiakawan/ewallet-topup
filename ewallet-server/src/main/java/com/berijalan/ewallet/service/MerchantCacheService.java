package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.response.ResMerchantDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantCacheService {

    private static final String CACHE_KEY = "merchants:active:all";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.merchants.ttl-hours:1}")
    private long ttlHours;

    public List<ResMerchantDto> get() {
        try {
            Object raw = redisTemplate.opsForValue().get(CACHE_KEY);
            if (raw == null) {
                log.debug("Cache miss: {}", CACHE_KEY);
                return null;
            }
            log.debug("Cache hit: {}", CACHE_KEY);
            return objectMapper.convertValue(raw, new TypeReference<List<ResMerchantDto>>() {});
        } catch (Exception e) {
            log.warn("Failed to read merchant cache, falling back to DB. Cause: {}", e.getMessage());
            return null;
        }
    }

    public void put(List<ResMerchantDto> merchants) {
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, merchants, Duration.ofHours(ttlHours));
            log.debug("Cached {} active merchants with TTL {}h", merchants.size(), ttlHours);
        } catch (Exception e) {
            log.warn("Failed to write merchant cache. Cause: {}", e.getMessage());
        }
    }

    public void evict() {
        try {
            redisTemplate.delete(CACHE_KEY);
            log.debug("Evicted merchant cache key: {}", CACHE_KEY);
        } catch (Exception e) {
            log.warn("Failed to evict merchant cache. Cause: {}", e.getMessage());
        }
    }
}
