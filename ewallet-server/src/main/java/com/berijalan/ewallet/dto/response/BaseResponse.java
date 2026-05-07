package com.berijalan.ewallet.dto.response;

public record BaseResponse<T>(
        String requestId,
        boolean status,
        String message,
        T data
) {}