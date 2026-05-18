package com.berijalan.ewallet.service;

import com.berijalan.ewallet.entity.constant.IdempotencyStatus;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.exception.ConflictException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String KEY_PREFIX = "idempotency";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.idempotency.ttl-hours:24}")
    private long ttlHours;

    /**
     * Memulai guard idempotency untuk request finansial.
     *
     * @param idempotencyKey kunci unik dari header request.
     * @param userId ID customer pemilik request.
     * @param endpoint method dan URI endpoint.
     * @param requestHash hash body request untuk mendeteksi reuse key dengan payload berbeda.
     * @return status proses baru atau replay response yang sudah selesai.
     */
    public IdempotencyResult start(String idempotencyKey, Long userId, String endpoint, String requestHash) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BadRequestException("Idempotency-Key header is required");
        }

        String redisKey = buildRedisKey(userId, endpoint, idempotencyKey);
        IdempotencyCacheEntry processingEntry = new IdempotencyCacheEntry(
                IdempotencyStatus.PROCESSING,
                requestHash,
                null,
                null
        );

        // WHY: setIfAbsent membuat hanya satu request dengan key yang sama boleh masuk ke proses finansial.
        Boolean created = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, serialize(processingEntry), Duration.ofHours(ttlHours));

        if (Boolean.TRUE.equals(created)) {
            return IdempotencyResult.processing(redisKey);
        }

        String existingValue = redisTemplate.opsForValue().get(redisKey);
        if (existingValue == null) {
            // WHY: Key bisa kedaluwarsa di antara setIfAbsent dan read; retry memulai guard baru secara aman.
            return start(idempotencyKey, userId, endpoint, requestHash);
        }

        IdempotencyCacheEntry existingEntry = deserialize(existingValue);
        if (!Objects.equals(existingEntry.requestHash(), requestHash)) {
            throw new ConflictException("Idempotency-Key already used for different request");
        }

        if (existingEntry.status() == IdempotencyStatus.PROCESSING) {
            throw new ConflictException("Request is still processing");
        }

        return IdempotencyResult.replay(
                redisKey,
                existingEntry.httpStatus(),
                existingEntry.responseBody()
        );
    }

    /**
     * Menyimpan response akhir agar retry dengan key yang sama bisa mendapatkan replay yang konsisten.
     *
     * @param redisKey key Redis guard idempotency.
     * @param requestHash hash body request asli.
     * @param httpStatus status HTTP response.
     * @param responseBody body response yang akan direplay.
     */
    public void complete(String redisKey, String requestHash, int httpStatus, String responseBody) {
        IdempotencyStatus status = httpStatus >= 200 && httpStatus < 300
                ? IdempotencyStatus.COMPLETED
                : IdempotencyStatus.FAILED;

        IdempotencyCacheEntry entry = new IdempotencyCacheEntry(
                status,
                requestHash,
                httpStatus,
                responseBody
        );

        redisTemplate.opsForValue().set(redisKey, serialize(entry), Duration.ofHours(ttlHours));
    }

    /**
     * Menghapus guard idempotency saat terjadi error tak terduga agar request dapat dicoba ulang.
     *
     * @param redisKey key Redis guard idempotency.
     */
    public void clear(String redisKey) {
        redisTemplate.delete(redisKey);
    }

    private String buildRedisKey(Long userId, String endpoint, String idempotencyKey) {
        // WHY: Scope key per user dan endpoint mencegah tabrakan antar operasi yang memakai nilai header sama.
        return KEY_PREFIX + ":" + userId + ":" + endpoint + ":" + idempotencyKey;
    }

    private String serialize(IdempotencyCacheEntry entry) {
        try {
            return objectMapper.writeValueAsString(entry);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize idempotency cache", ex);
        }
    }

    private IdempotencyCacheEntry deserialize(String value) {
        try {
            return objectMapper.readValue(value, IdempotencyCacheEntry.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize idempotency cache", ex);
        }
    }

    private record IdempotencyCacheEntry(
            IdempotencyStatus status,
            String requestHash,
            Integer httpStatus,
            String responseBody
    ) {
    }

    public record IdempotencyResult(
            boolean replay,
            String redisKey,
            Integer httpStatus,
            String responseBody
    ) {
        public static IdempotencyResult processing(String redisKey) {
            return new IdempotencyResult(false, redisKey, null, null);
        }

        public static IdempotencyResult replay(String redisKey, Integer httpStatus, String responseBody) {
            return new IdempotencyResult(true, redisKey, httpStatus, responseBody);
        }
    }
}
