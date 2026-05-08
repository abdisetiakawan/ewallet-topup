package com.berijalan.ewallet.dto.request;

import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ReqTransactionHistoryDto(
        @Min(value = 0, message = "Page must not be negative") Integer page,
        @Min(value = 1, message = "Size minimum is 1") @Max(value = 100, message = "Size maximum is 100") Integer size,
        TransactionStatus status,
        TransactionType type
) implements PageableRequest {

    public ReqTransactionHistoryDto {
        if (page == null) page = 0;
        if (size == null) size = 10;
    }
}
