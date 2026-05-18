package com.berijalan.ewallet.exception;

import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.dto.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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

        // WHY: Validation error memakai map field-message agar client bisa menampilkan error per input.
        return error(HttpStatus.BAD_REQUEST, "Validation Error", errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Class<?> requiredType = ex.getRequiredType();
        String message = requiredType != null && requiredType.isEnum()
                ? "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'. Allowed: " + Arrays.toString(requiredType.getEnumConstants())
                : "Invalid value for parameter '" + ex.getName() + "'";

        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getName(), message);

        return error(HttpStatus.BAD_REQUEST, "Validation Error", errors);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<BaseResponse<Void>> handleBadRequestException(BadRequestException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler({UnauthorizedException.class, AuthenticationException.class})
    public ResponseEntity<BaseResponse<Void>> handleUnauthorizedException(Exception ex) {
        String message = ex.getMessage() != null && !ex.getMessage().isEmpty() ? ex.getMessage() : "Unauthorized";
        return error(HttpStatus.UNAUTHORIZED, message, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "Access Denied", null);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNotFoundException(NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<BaseResponse<Void>> handleConflictException(ConflictException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGlobalException(Exception ex) {
        // WHY: Detail exception tidak dikirim ke client agar informasi internal tidak bocor.
        log.error("Internal Server Error", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null);
    }

    private <T> ResponseEntity<BaseResponse<T>> error(HttpStatus status, String message, T data) {
        return ResponseEntity
                .status(status)
                .body(ApiResponseFactory.error(message, data));
    }
}
