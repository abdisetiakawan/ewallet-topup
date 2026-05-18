package com.berijalan.ewallet.dto.request;

import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Query parameter untuk riwayat transaksi customer.")
public record ReqTransactionHistoryDto(
        @Schema(description = "Nomor halaman berbasis nol.", example = "0", defaultValue = "0", minimum = "0")
        @Min(value = 0, message = "Page must not be negative") Integer page,
        @Schema(description = "Jumlah item per halaman.", example = "10", defaultValue = "10", minimum = "1", maximum = "100")
        @Min(value = 1, message = "Size minimum is 1") @Max(value = 100, message = "Size maximum is 100") Integer size,
        @Schema(description = "Filter status transaksi.", example = "SUCCESS")
        TransactionStatus status,
        @Schema(description = "Filter tipe transaksi.", example = "PAYMENT")
        TransactionType type
) implements PageableRequest {

    public ReqTransactionHistoryDto {
        if (page == null) page = 0;
        if (size == null) size = 10;
    }
}
