package com.berijalan.ewallet.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    @Value("${ewallet.app.jwtSecret:======================EwalletTopupSecretKeyForJwt2026======================}")
    private String jwtSecret;

    @Value("${app.jwt.access-token.ttl-minutes:15}")
    private long accessTokenTtlMinutes;

    /**
     * Membuat JWT untuk principal yang berhasil login.
     *
     * @param authentication hasil autentikasi Spring Security.
     * @return access token JWT.
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return generateTokenForUserId(userPrincipal.getId());
    }

    /**
     * Membuat JWT berdasarkan userId untuk flow login dan refresh token.
     *
     * @param userId ID user yang menjadi subject token.
     * @return access token JWT.
     */
    public String generateTokenForUserId(Long userId) {
        long expirationMs = accessTokenTtlMinutes * 60 * 1000;
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Mengembalikan TTL access token dalam detik untuk response API.
     *
     * @return masa berlaku access token dalam detik.
     */
    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlMinutes * 60;
    }

    private Key key() {
        // WHY: Secret disimpan Base64 agar kompatibel dengan jjwt HMAC key material.
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Membaca subject userId dari JWT yang sudah tervalidasi.
     *
     * @param token access token JWT.
     * @return userId dalam bentuk string.
     */
    public String getUserIdFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Memvalidasi signature dan struktur JWT.
     *
     * @param authToken access token dari header Authorization.
     * @return true jika token valid.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (JwtException e) {
            log.error("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }
}
