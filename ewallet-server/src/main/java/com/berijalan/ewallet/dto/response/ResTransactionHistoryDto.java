package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response halaman riwayat transaksi.")
public record ResTransactionHistoryDto(
        @Schema(description = "Daftar transaksi pada halaman saat ini.")
        List<ResTransactionItemDto> content,
        @Schema(description = "Nomor halaman berbasis nol.", example = "0")
        int page,
        @Schema(description = "Jumlah item per halaman.", example = "10")
        int size,
        @Schema(description = "Total transaksi yang cocok dengan filter.", example = "25")
        long totalElements,
        @Schema(description = "Total halaman yang tersedia.", example = "3")
        int totalPages
) {}
