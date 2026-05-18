package com.berijalan.ewallet.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@EnableCaching
@Configuration
public class RedisConfig {

    @Value("${app.cache.merchants.ttl-hours:1}")
    private long merchantsTtlHours;

    private GenericJackson2JsonRedisSerializer buildSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // WHY: Cache menyimpan beberapa tipe DTO, sehingga type metadata diperlukan saat value dibaca kembali.
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    /**
     * Menyediakan cache manager Redis untuk data read-heavy seperti daftar merchant aktif.
     *
     * @param connectionFactory koneksi Redis yang dipakai Spring Cache.
     * @return cache manager dengan logging hit/miss.
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer jsonSerializer = buildSerializer();
        // WHY: TTL merchant menyeimbangkan performa daftar merchant dengan risiko data pajak yang stale.
        RedisCacheConfiguration merchantsConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(merchantsTtlHours))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer));

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(merchantsConfig)
                .build();
        return new LoggingCacheManager(redisCacheManager);
    }

    /**
     * Menyediakan RedisTemplate utama untuk cache wallet, idempotency, refresh token, dan email change token.
     *
     * @param connectionFactory koneksi Redis aplikasi.
     * @return RedisTemplate dengan serializer JSON yang mendukung Java time.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        try (var conn = connectionFactory.getConnection()) {
            conn.ping();
        } catch (Exception e) {
            // WHY: Redis menyimpan idempotency dan token, jadi aplikasi lebih aman gagal start daripada berjalan parsial.
            throw new IllegalStateException("Failed to connect to Redis. Redis connection is mandatory.", e);
        }

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        GenericJackson2JsonRedisSerializer jsonSerializer = buildSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
