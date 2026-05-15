package com.berijalan.ewallet.common.web;

import com.berijalan.ewallet.config.MdcFilter;
import com.berijalan.ewallet.dto.response.BaseResponse;
import org.slf4j.MDC;

import java.util.UUID;

public final class ApiResponseFactory {

    private ApiResponseFactory() {
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(requestId(), true, message, data);
    }

    public static BaseResponse<Void> success(String message) {
        return success(message, null);
    }

    public static <T> BaseResponse<T> error(String message, T data) {
        return new BaseResponse<>(requestId(), false, message, data);
    }

    public static BaseResponse<Void> error(String message) {
        return error(message, null);
    }

    public static String requestId() {
        String requestId = MDC.get(MdcFilter.REQUEST_ID);
        return requestId != null ? requestId : "req-" + UUID.randomUUID();
    }
}
