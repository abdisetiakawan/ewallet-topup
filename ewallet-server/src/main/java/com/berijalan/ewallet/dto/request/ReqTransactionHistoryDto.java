package com.berijalan.ewallet.dto.request;

import com.berijalan.ewallet.entity.constant.TransactionStatus;
import com.berijalan.ewallet.entity.constant.TransactionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class ReqTransactionHistoryDto {

    @Min(value = 0, message = "Page must not be negative")
    private Integer page = 0;

    @Min(value = 1, message = "Size minimum is 1")
    @Max(value = 100, message = "Size maximum is 100")
    private Integer size = 10;

    private TransactionStatus status;

    private TransactionType type;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}
