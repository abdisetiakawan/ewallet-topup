package com.berijalan.ewallet.repository;

import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @EntityGraph(attributePaths = {"merchant", "user"})
    Page<Transaction> findByUser(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"merchant", "user"})
    Page<Transaction> findByUserAndStatus(User user, TransactionStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"merchant", "user"})
    Page<Transaction> findByUserAndType(User user, TransactionType type, Pageable pageable);

    @EntityGraph(attributePaths = {"merchant", "user"})
    Page<Transaction> findByUserAndStatusAndType(
            User user,
            TransactionStatus status,
            TransactionType type,
            Pageable pageable);

    boolean existsByReferenceId(String referenceId);
}
