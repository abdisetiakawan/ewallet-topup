package com.berijalan.ewallet.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Merchant tujuan pembayaran customer.
 */
@Entity
@Table(name = "mst_merchants")
@Getter @Setter @NoArgsConstructor
public class Merchant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * Merchant nonaktif tetap disimpan untuk audit transaksi lama, tetapi tidak muncul pada daftar customer.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "merchant", fetch = FetchType.LAZY)
    private List<MerchantTax> taxes = new ArrayList<>();
}
