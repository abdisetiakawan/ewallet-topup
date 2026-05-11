package com.berijalan.ewallet.config;

import com.berijalan.ewallet.dto.response.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdempotencyHeaderFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (requiresIdempotencyKey(request) && isBlank(request.getHeader(IDEMPOTENCY_KEY_HEADER))) {
            BaseResponse<Void> responseBody = new BaseResponse<>(
                    getRequestId(),
                    false,
                    "Idempotency-Key header is required",
                    null
            );

            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(responseBody));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresIdempotencyKey(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        return "POST".equalsIgnoreCase(method)
                && ("/api/transactions/pay".equals(path) || "/api/wallet/topup".equals(path));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String getRequestId() {
        String requestId = MDC.get(MdcFilter.REQUEST_ID);
        return requestId != null ? requestId : "req-" + UUID.randomUUID();
    }
}
