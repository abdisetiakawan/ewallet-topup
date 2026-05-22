package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Preview pembayaran merchant berdasarkan konfigurasi pajak aktif terkini.")
public record ResPaymentQuoteDto(
        @Schema(description = "Nama merchant tujuan pembayaran.", example = "Gopay")
        String merchantName,
        @Schema(description = "Nominal pembayaran sebelum pajak.", example = "100000")
        Long baseAmount,
        @Schema(description = "Total pajak yang akan ditambahkan ke pembayaran.", example = "2500")
        Long taxAmount,
        @Schema(description = "Total nominal pembayaran setelah pajak.", example = "102500")
        Long amount,
        @Schema(description = "Rincian pajak aktif yang dihitung untuk preview.")
        List<TaxSnapshotDto> taxDetails
) {}
