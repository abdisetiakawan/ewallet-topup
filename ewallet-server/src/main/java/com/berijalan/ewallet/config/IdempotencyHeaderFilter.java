package com.berijalan.ewallet.config;

import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.contract.model.BaseResponseVoid;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class IdempotencyHeaderFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final ObjectMapper objectMapper;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * Menolak request finansial tanpa Idempotency-Key sebelum masuk ke controller.
     *
     * @param request HTTP request.
     * @param response HTTP response.
     * @param filterChain filter berikutnya.
     * @throws ServletException jika handler endpoint gagal di-resolve.
     * @throws IOException jika response error gagal ditulis.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (requiresIdempotencyKey(request) && isBlank(request.getHeader(IDEMPOTENCY_KEY_HEADER))) {
            // WHY: Validasi di filter memberi error konsisten sebelum aspect membuat state PROCESSING di Redis.
            BaseResponseVoid responseBody = ApiResponseFactory.error("Idempotency-Key header is required");

            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(responseBody));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresIdempotencyKey(HttpServletRequest request) throws ServletException {
        try {
            // WHY: Requirement mengikuti anotasi endpoint agar daftar endpoint idempoten tetap satu sumber.
            HandlerExecutionChain handlerExecutionChain = requestMappingHandlerMapping.getHandler(request);

            if (handlerExecutionChain == null
                    || !(handlerExecutionChain.getHandler() instanceof HandlerMethod handlerMethod)) {
                return false;
            }

            return handlerMethod.hasMethodAnnotation(IdempotencyGuarded.class);
        } catch (Exception ex) {
            throw new ServletException("Failed to resolve request handler", ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
