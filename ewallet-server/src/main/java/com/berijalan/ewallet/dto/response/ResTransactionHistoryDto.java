package com.berijalan.ewallet.dto.response;

import java.util.List;

public record ResTransactionHistoryDto(
        List<ResTransactionItemDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}