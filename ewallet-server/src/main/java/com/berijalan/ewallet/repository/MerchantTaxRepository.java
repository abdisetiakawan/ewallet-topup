package com.berijalan.ewallet.repository;

import com.berijalan.ewallet.entity.MerchantTax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantTaxRepository extends JpaRepository<MerchantTax, Long> {
    List<MerchantTax> findByMerchantIdAndIsActiveTrue(Long merchantId);
}
