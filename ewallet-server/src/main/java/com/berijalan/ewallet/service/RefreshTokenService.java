package com.berijalan.ewallet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

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

        // Cross-check that the user's active token matches
        String activeToken = redisTemplate.opsForValue().get(buildUserKey(Long.parseLong(userIdStr)));
        if (!token.equals(activeToken)) {
            return null;
        }

        return Long.parseLong(userIdStr);
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
