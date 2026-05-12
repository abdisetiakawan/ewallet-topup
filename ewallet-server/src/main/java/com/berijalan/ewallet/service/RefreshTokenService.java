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
     * Creates a refresh token for a user.
     * Stores two Redis entries:
     *   - token → userId (for lookup during refresh)
     *   - userId → token (for revocation during logout)
     */
    public String create(Long userId) {
        // Revoke existing token first (single-session policy)
        revoke(userId);

        String token = UUID.randomUUID().toString();
        Duration ttl = Duration.ofDays(ttlDays);

        redisTemplate.opsForValue().set(buildTokenKey(token), String.valueOf(userId), ttl);
        redisTemplate.opsForValue().set(buildUserKey(userId), token, ttl);

        return token;
    }

    /**
     * Validates the refresh token and returns the associated userId.
     * Returns null if the token is invalid or expired.
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

            // Cross-check that the user's active token matches
            String activeToken = redisTemplate.opsForValue().get(buildUserKey(userId));
            if (!token.equals(activeToken)) {
                return null;
            }

            return userId;
        } catch (NumberFormatException e) {
            log.warn("Corrupted userId value in Redis for token lookup: '{}' — treating as invalid token", userIdStr);
            return null;
        }
    }

    /**
     * Revokes the refresh token for a user.
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
