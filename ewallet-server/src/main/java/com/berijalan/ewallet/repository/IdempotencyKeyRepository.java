package com.berijalan.ewallet.repository;

import com.berijalan.ewallet.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Integer> {
    Optional<IdempotencyKey> findByUserIdAndEndpointAndIdempotencyKey(
            Long userId,
            String endpoint,
            String idempotencyKey
    );
}
