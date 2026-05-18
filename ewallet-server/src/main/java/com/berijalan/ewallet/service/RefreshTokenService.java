package com.berijalan.ewallet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String TOKEN_KEY_PREFIX = "refresh_token:token";
    private static final String USER_KEY_PREFIX = "refresh_token:user";

    private final StringRedisTemplate redisTemplate;

    @Value("${app.jwt.refresh-token.ttl-days:7}")
    private long ttlDays;

    /**
     * Membuat refresh token baru dan mencabut token lama milik user.
     *
     * @param userId ID user pemilik sesi.
     * @return refresh token baru untuk cookie HTTP-only.
     */
    public String create(Long userId) {
        // WHY: Single-session policy mencegah refresh token lama tetap hidup setelah login baru.
        revoke(userId);

        String token = UUID.randomUUID().toString();
        Duration ttl = Duration.ofDays(ttlDays);

        redisTemplate.opsForValue().set(buildTokenKey(token), String.valueOf(userId), ttl);
        redisTemplate.opsForValue().set(buildUserKey(userId), token, ttl);

        return token;
    }

    /**
     * Memvalidasi refresh token dan mengembalikan userId pemilik token.
     *
     * @param token refresh token dari cookie.
     * @return userId jika token aktif, atau null jika token tidak valid.
     */
    public Long validateAndGetUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        String userIdStr = redisTemplate.opsForValue().get(buildTokenKey(token));
        if (userIdStr == null) {
            return null;
        }

        try {
            long userId = Long.parseLong(userIdStr);

            // WHY: Pemeriksaan user key memastikan token lama tidak bisa dipakai setelah sesi dicabut.
            String activeToken = redisTemplate.opsForValue().get(buildUserKey(userId));
            if (!token.equals(activeToken)) {
                return null;
            }

            return userId;
        } catch (NumberFormatException e) {
            log.warn("Corrupted userId value in Redis for token lookup: '{}' - treating as invalid token", userIdStr);
            return null;
        }
    }

    /**
     * Mencabut refresh token aktif milik user.
     *
     * @param userId ID user yang sesinya dicabut.
     */
    public void revoke(Long userId) {
        String existingToken = redisTemplate.opsForValue().get(buildUserKey(userId));
        if (existingToken != null) {
            redisTemplate.delete(buildTokenKey(existingToken));
        }
        redisTemplate.delete(buildUserKey(userId));
    }

    private String buildTokenKey(String token) {
        return TOKEN_KEY_PREFIX + ":" + token;
    }

    private String buildUserKey(Long userId) {
        return USER_KEY_PREFIX + ":" + userId;
    }
}
