package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.response.ResEmailChangeDto;
import com.berijalan.ewallet.dto.response.ResUserSummaryDto;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.exception.ConflictException;
import com.berijalan.ewallet.exception.NotFoundException;
import com.berijalan.ewallet.mapper.UserMapper;
import com.berijalan.ewallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEmailChangeService {

    private static final String TOKEN_KEY_PREFIX = "email_change:token:";
    private static final String USER_KEY_PREFIX  = "email_change:user:";

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final MailSenderService mailSenderService;
    private final UserMapper userMapper;

    @Value("${app.email-change.ttl-minutes:15}")
    private int ttlMinutes;

    /**
     * Membuat token verifikasi untuk email baru customer.
     *
     * @param userId ID customer dari JWT.
     * @param newEmail email baru yang akan diverifikasi.
     * @return email tujuan dan masa berlaku token.
     * @throws ConflictException jika email baru sudah digunakan.
     */
    public ResEmailChangeDto requestEmailChange(Long userId, String newEmail) {
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new ConflictException("Email is already in use");
        }

        // WHY: Hanya satu token aktif per user agar konfirmasi lama tidak menimpa permintaan terbaru.
        revokePendingRequest(userId);

        String token = UUID.randomUUID().toString();
        Duration ttl = Duration.ofMinutes(ttlMinutes);

        redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + token, userId + ":" + newEmail, ttl);
        redisTemplate.opsForValue().set(USER_KEY_PREFIX + userId, token, ttl);

        mailSenderService.sendEmailChangeToken(newEmail, token, ttlMinutes);

        log.info("Email change requested for userId={} to newEmail={}", userId, newEmail);
        return new ResEmailChangeDto(newEmail, ttlMinutes);
    }

    /**
     * Mengonfirmasi token email change dan menyimpan email baru secara transaksional.
     *
     * @param userId ID customer dari JWT.
     * @param token token verifikasi dari email baru.
     * @return profil setelah email berhasil diganti.
     * @throws BadRequestException jika token salah, kedaluwarsa, atau bukan milik user.
     * @throws ConflictException jika email baru sudah dipakai sebelum token dikonfirmasi.
     * @throws NotFoundException jika user tidak ditemukan.
     */
    @Transactional
    public ResUserSummaryDto confirmEmailChange(Long userId, String token) {
        String value = redisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + token);
        if (value == null) {
            throw new BadRequestException("Invalid or expired token");
        }

        String[] parts = value.split(":", 2);
        Long tokenUserId = Long.parseLong(parts[0]);
        String newEmail  = parts[1];

        if (!tokenUserId.equals(userId)) {
            throw new BadRequestException("Invalid or expired token");
        }

        String storedToken = redisTemplate.opsForValue().get(USER_KEY_PREFIX + userId);
        if (!token.equals(storedToken)) {
            throw new BadRequestException("Invalid or expired token");
        }

        // WHY: Email bisa didaftarkan user lain saat token masih berlaku, jadi perlu dicek ulang sebelum commit.
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new ConflictException("Email is already in use");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setEmail(newEmail);
        userRepository.save(user);

        redisTemplate.delete(TOKEN_KEY_PREFIX + token);
        redisTemplate.delete(USER_KEY_PREFIX + userId);

        log.info("Email changed successfully for userId={} to newEmail={}", userId, newEmail);

        return userMapper.toSummaryDto(user);
    }

    private void revokePendingRequest(Long userId) {
        String existingToken = redisTemplate.opsForValue().get(USER_KEY_PREFIX + userId);
        if (existingToken != null) {
            redisTemplate.delete(TOKEN_KEY_PREFIX + existingToken);
            redisTemplate.delete(USER_KEY_PREFIX + userId);
            log.debug("Revoked previous email change request for userId={}", userId);
        }
    }
}
