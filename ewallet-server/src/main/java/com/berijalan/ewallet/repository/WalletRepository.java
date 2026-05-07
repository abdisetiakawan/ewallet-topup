package com.berijalan.ewallet.repository;

import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {
    Optional<Wallet> findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w join fetch w.user where w.user.email = :email")
    Optional<Wallet> findByUserEmailForUpdate(@Param("email") String email);
}
