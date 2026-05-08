package com.berijalan.ewallet.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class ReqTransactionHistoryDto {

    @Min(value = 0, message = "Page must not be negative")
    private Integer page = 0;

    @Min(value = 1, message = "Size minimum is 1")
    @Max(value = 100, message = "Size maximum is 100")
    private Integer size = 10;

    private String status;

    private String type;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
