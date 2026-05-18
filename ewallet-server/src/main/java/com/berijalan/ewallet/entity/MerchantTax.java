package com.berijalan.ewallet.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import com.berijalan.ewallet.entity.constant.TaxType;
import com.berijalan.ewallet.entity.constant.TaxValueType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Konfigurasi pajak merchant yang dihitung saat customer melakukan pembayaran.
 */
@Entity
@Table(name = "mst_merchant_taxes")
@Getter @Setter @NoArgsConstructor
public class MerchantTax extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "tax_name", nullable = false, length = 100)
    private String taxName;

    /**
     * Kategori bisnis pajak. Service admin memastikan hanya satu pajak aktif per kategori.
     */
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "tax_type", columnDefinition = "tax_type_enum", nullable = false)
    private TaxType taxType;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "value_type", columnDefinition = "tax_value_type_enum", nullable = false)
    private TaxValueType valueType;

    @Column(name = "tax_value", nullable = false, precision = 10, scale = 4)
    private BigDecimal taxValue;

    /**
     * Pajak nonaktif tetap dipertahankan agar perubahan konfigurasi tidak menghapus histori audit.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Mencatat waktu mulai berlaku pajak sebagai metadata audit konfigurasi biaya.
     */
    @Column(name = "effective_at", nullable = false)
    private LocalDateTime effectiveAt;

    /**
     * Kosong berarti pajak belum memiliki tanggal akhir berlaku.
     */
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
}
