package com.berijalan.ewallet.repository;

import com.berijalan.ewallet.entity.Transaction;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.constant.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    
    Page<Transaction> findByUserAndStatus(User user, TransactionStatus status, Pageable pageable);
    
    Page<Transaction> findByUser(User user, Pageable pageable);

    boolean existsByReferenceId(String referenceId);
}