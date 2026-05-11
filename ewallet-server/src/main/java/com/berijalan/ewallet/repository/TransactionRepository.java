package com.berijalan.ewallet.repository;

import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @EntityGraph(attributePaths = {"merchant", "user"})
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:type IS NULL OR t.type = :type)")
    Page<Transaction> findByUserWithFilters(
            @Param("user") User user,
            @Param("status") TransactionStatus status,
            @Param("type") TransactionType type,
            Pageable pageable);

    boolean existsByReferenceId(String referenceId);
}
