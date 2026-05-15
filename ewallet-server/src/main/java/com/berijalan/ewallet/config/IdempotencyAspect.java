package com.berijalan.ewallet.config;

import com.berijalan.ewallet.common.security.CurrentUser;
import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.service.IdempotencyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;

@Aspect
@Component
@RequiredArgsConstructor
public class IdempotencyAspect {

    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.berijalan.ewallet.config.IdempotencyGuarded)")
    public Object guardFinancialRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest httpRequest = currentHttpRequest();
        Authentication authentication = findArgument(joinPoint.getArgs(), Authentication.class);
        Long userId = CurrentUser.id(authentication);

        Object requestBody = findRequestBody(joinPoint.getArgs());
        String requestHash = DigestUtils.md5DigestAsHex(
                objectMapper.writeValueAsString(requestBody).getBytes(StandardCharsets.UTF_8)
        );
        String endpoint = httpRequest.getMethod() + ":" + httpRequest.getRequestURI();
        String idempotencyKey = httpRequest.getHeader("Idempotency-Key");

        IdempotencyService.IdempotencyResult result = idempotencyService.start(
                idempotencyKey,
                userId,
                endpoint,
                requestHash
        );

        if (result.replay()) {
            JsonNode responseBody = objectMapper.readTree(result.responseBody());
            return ResponseEntity
                    .status(result.httpStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseBody);
        }

        try {
            ResponseEntity<?> response = (ResponseEntity<?>) joinPoint.proceed();
            idempotencyService.complete(
                    result.redisKey(),
                    requestHash,
                    response.getStatusCode().value(),
                    objectMapper.writeValueAsString(response.getBody())
            );
            return response;
        } catch (BadRequestException ex) {
            ResponseEntity<BaseResponse<Void>> response = ResponseEntity
                    .badRequest()
                    .body(ApiResponseFactory.error(ex.getMessage()));

            idempotencyService.complete(
                    result.redisKey(),
                    requestHash,
                    response.getStatusCode().value(),
                    objectMapper.writeValueAsString(response.getBody())
            );

            return response;
        } catch (Throwable ex) {
            idempotencyService.clear(result.redisKey());
            throw ex;
        }
    }

    private HttpServletRequest currentHttpRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    private <T> T findArgument(Object[] args, Class<T> type) {
        for (Object arg : args) {
            if (type.isInstance(arg)) {
                return type.cast(arg);
            }
        }
        throw new IllegalStateException("Required argument not found: " + type.getSimpleName());
    }

    private Object findRequestBody(Object[] args) {
        for (Object arg : args) {
            if (!(arg instanceof Authentication)) {
                return arg;
            }
        }
        throw new IllegalStateException("Request body not found");
    }
}
