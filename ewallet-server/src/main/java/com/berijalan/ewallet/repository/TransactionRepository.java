package com.berijalan.ewallet.repository;

import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * EntityGraph memuat merchant dan user bersama transaksi agar riwayat tidak memicu N+1 query saat mapping DTO.
     */
    @EntityGraph(attributePaths = {"merchant", "user"})
    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"merchant", "user"})
    Page<Transaction> findByUserIdAndStatus(Long userId, TransactionStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"merchant", "user"})
    Page<Transaction> findByUserIdAndType(Long userId, TransactionType type, Pageable pageable);

    @EntityGraph(attributePaths = {"merchant", "user"})
    Page<Transaction> findByUserIdAndStatusAndType(
            Long userId,
            TransactionStatus status,
            TransactionType type,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "merchant")
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    boolean existsByReferenceId(String referenceId);
}
