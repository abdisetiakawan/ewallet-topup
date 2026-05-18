package com.berijalan.ewallet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representasi saldo customer yang menjadi sumber kebenaran untuk top-up dan pembayaran.
 */
@Entity
@Table(name = "mst_wallets")
@Getter @Setter @NoArgsConstructor
public class Wallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    /**
     * Saldo disimpan sebagai integer minor unit agar operasi finansial tidak terkena pembulatan floating point.
     */
    @Column(nullable = false)
    private Long balance;
}
