package com.berijalan.ewallet.repository;

import com.berijalan.ewallet.entity.Merchant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByName(String name);

    /**
     * Memuat taxes bersama merchant aktif agar daftar merchant customer dapat dicache tanpa lazy-loading tambahan.
     */
    @EntityGraph(attributePaths = {"taxes"})
    List<Merchant> findByIsActiveTrue();
}
