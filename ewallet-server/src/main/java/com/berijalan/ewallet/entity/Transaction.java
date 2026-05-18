package com.berijalan.ewallet.entity;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catatan transaksi wallet yang menyimpan nominal, audit saldo, dan snapshot biaya saat transaksi terjadi.
 */
@Entity
@Table(name = "trx_transactions", indexes = {
    @Index(name = "idx_transactions_user_id", columnList = "user_id"),
    @Index(name = "idx_transactions_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_transactions_reference_id", columnList = "reference_id", unique = true),
    @Index(name = "idx_transactions_user_status_date", columnList = "user_id, status, created_at DESC"),
    @Index(name = "idx_transactions_created_at", columnList = "created_at")
})
@Getter @Setter @NoArgsConstructor
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    @Column(nullable = false)
    private Long amount;

    /**
     * Nominal transaksi sebelum pajak agar audit dapat membedakan harga dasar dan biaya tambahan merchant.
     */
    @Column(name = "base_amount", nullable = false)
    private Long baseAmount;

    /**
     * Total pajak yang dihitung pada saat transaksi dibuat.
     */
    @Column(name = "tax_amount", nullable = false)
    private Long taxAmount = 0L;

    /**
     * Snapshot JSON menjaga audit pembayaran tetap benar walau konfigurasi pajak merchant berubah setelah transaksi.
     */
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "tax_snapshot", columnDefinition = "jsonb")
    private String taxSnapshot;

    /**
     * Saldo sebelum transaksi untuk rekonsiliasi dan investigasi anomali saldo.
     */
    @Column(name = "balance_before", nullable = false)
    private Long balanceBefore = 0L;

    /**
     * Saldo setelah transaksi untuk rekonsiliasi dan response API tanpa menghitung ulang.
     */
    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter = 0L;

    @Column(length = 255)
    private String description;

    /**
     * Menggunakan enum PostgreSQL agar nilai transaksi di database tetap sejalan dengan domain aplikasi.
     */
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "transaction_type", nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "transaction_status", nullable = false)
    private TransactionStatus status;

    /**
     * ID referensi eksternal yang aman ditampilkan ke client tanpa membuka ID database berurutan.
     */
    @Column(name = "reference_id", nullable = false, unique = true)
    private String referenceId;
}
