package com.berijalan.ewallet.config;

import com.berijalan.ewallet.logging.LoggingSupport;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID = LoggingSupport.REQUEST_ID;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String requestId = resolveRequestId(request);
            MDC.put(LoggingSupport.REQUEST_ID, requestId);
            MDC.put(LoggingSupport.HTTP_METHOD, request.getMethod());
            MDC.put(LoggingSupport.REQUEST_PATH, request.getRequestURI());
            response.setHeader(LoggingSupport.REQUEST_ID_HEADER, requestId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(LoggingSupport.REQUEST_ID);
            MDC.remove(LoggingSupport.HTTP_METHOD);
            MDC.remove(LoggingSupport.REQUEST_PATH);
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(LoggingSupport.REQUEST_ID_HEADER);
        return requestId != null && !requestId.isBlank()
                ? requestId
                : "req-" + UUID.randomUUID();
    }
}
