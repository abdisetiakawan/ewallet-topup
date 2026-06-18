package com.berijalan.ewallet.config;

import com.berijalan.ewallet.common.security.CurrentUser;
import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.contract.model.BaseResponseVoid;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.service.IdempotencyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;

@Aspect
@Component
@RequiredArgsConstructor
public class IdempotencyAspect {

    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    /**
     * Menjaga endpoint finansial agar retry dengan Idempotency-Key yang sama tidak mengeksekusi mutasi ulang.
     *
     * @param joinPoint method controller yang diberi {@link IdempotencyGuarded}.
     * @return response baru atau replay response yang sudah tersimpan.
     * @throws Throwable jika eksekusi endpoint gagal di luar error bisnis yang dapat direplay.
     */
    @Around("@annotation(com.berijalan.ewallet.config.IdempotencyGuarded)")
    public Object guardFinancialRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest httpRequest = currentHttpRequest();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = CurrentUser.id(authentication);

        Object requestBody = findRequestBody(joinPoint);
        // WHY: Hash body mencegah client memakai Idempotency-Key yang sama untuk mutasi finansial berbeda.
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
            // WHY: Response replay mempertahankan kontrak idempotency tanpa memotong saldo atau top-up ulang.
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
            // WHY: Error bisnis 400 direplay agar retry request invalid tidak terus menyentuh service finansial.
            ResponseEntity<BaseResponseVoid> response = ResponseEntity
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
            // WHY: Error tak terduga tidak dicache agar client dapat retry setelah akar masalah selesai.
            idempotencyService.clear(result.redisKey());
            throw ex;
        }
    }

    private HttpServletRequest currentHttpRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    private Object findRequestBody(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Annotation[][] parameterAnnotations = signature.getMethod().getParameterAnnotations();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof RequestBody) {
                    return args[i];
                }
            }
        }

        for (Object arg : args) {
            if (arg != null && !(arg instanceof String) && !(arg instanceof Authentication)) {
                return arg;
            }
        }

        throw new IllegalStateException("Request body not found");
    }
}
