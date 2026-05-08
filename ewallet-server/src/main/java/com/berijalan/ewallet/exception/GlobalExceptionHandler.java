package com.berijalan.ewallet.exception;

import com.berijalan.ewallet.config.MdcFilter;
import com.berijalan.ewallet.dto.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private String getRequestId() {
        String requestId = MDC.get(MdcFilter.REQUEST_ID);
        if (requestId == null) {
            requestId = "req-" + UUID.randomUUID().toString();
        }
        return requestId;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return buildValidationResponse(ex.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleBindExceptions(BindException ex) {
        return buildValidationResponse(ex.getBindingResult().getFieldErrors());
    }

    private ResponseEntity<BaseResponse<Map<String, String>>> buildValidationResponse(Iterable<FieldError> fieldErrors) {
        Map<String, String> errors = new HashMap<>();
        fieldErrors.forEach((error) -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        BaseResponse<Map<String, String>> response = new BaseResponse<>(
                getRequestId(),
                false,
                "Validation Error",
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Class<?> requiredType = ex.getRequiredType();
        String message = requiredType != null && requiredType.isEnum()
                ? "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'. Allowed: " + Arrays.toString(requiredType.getEnumConstants())
                : "Invalid value for parameter '" + ex.getName() + "'";

        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getName(), message);

        BaseResponse<Map<String, String>> response = new BaseResponse<>(
                getRequestId(),
                false,
                "Validation Error",
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<BaseResponse<Void>> handleBadRequestException(BadRequestException ex) {
        BaseResponse<Void> response = new BaseResponse<>(
                getRequestId(),
                false,
                ex.getMessage(),
                null
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UnauthorizedException.class, AuthenticationException.class})
    public ResponseEntity<BaseResponse<Void>> handleUnauthorizedException(Exception ex) {
        BaseResponse<Void> response = new BaseResponse<>(
                getRequestId(),
                false,
                ex.getMessage() != null && !ex.getMessage().isEmpty() ? ex.getMessage() : "Unauthorized",
                null
        );

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        BaseResponse<Void> response = new BaseResponse<>(
                getRequestId(),
                false,
                "Access Denied",
                null
        );

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGlobalException(Exception ex) {
        log.error("Internal Server Error", ex);
        BaseResponse<Void> response = new BaseResponse<>(
                getRequestId(),
                false,
                "Internal Server Error: " + ex.getMessage(),
                null
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
