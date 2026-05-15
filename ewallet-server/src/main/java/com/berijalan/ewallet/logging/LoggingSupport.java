package com.berijalan.ewallet.logging;

import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.exception.ConflictException;
import com.berijalan.ewallet.exception.NotFoundException;
import com.berijalan.ewallet.exception.UnauthorizedException;
import com.berijalan.ewallet.security.UserDetailsImpl;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.aspectj.lang.reflect.MethodSignature;

public final class LoggingSupport {

    public static final String REQUEST_ID = "requestId";
    public static final String HTTP_METHOD = "httpMethod";
    public static final String REQUEST_PATH = "requestPath";
    public static final String USER_ID = "userId";
    public static final String ANONYMOUS_USER = "anonymous";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private LoggingSupport() {
    }

    public static String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ANONYMOUS_USER;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return String.valueOf(userDetails.getId());
        }

        return ANONYMOUS_USER;
    }

    public static MdcScope withUserId() {
        return new MdcScope(USER_ID, currentUserId());
    }

    public static MdcScope withUserId(MethodSignature signature, Object[] args) {
        return new MdcScope(USER_ID, resolveUserId(signature, args));
    }

    public static String loggedUserId() {
        String userId = MDC.get(USER_ID);
        return userId != null ? userId : currentUserId();
    }

    public static boolean isExpectedException(Throwable throwable) {
        return throwable instanceof BadRequestException
                || throwable instanceof UnauthorizedException
                || throwable instanceof AccessDeniedException
                || throwable instanceof NotFoundException
                || throwable instanceof ConflictException
                || throwable instanceof MethodArgumentNotValidException
                || throwable instanceof BindException
                || throwable instanceof MethodArgumentTypeMismatchException;
    }

    public static int resolveHttpStatus(Throwable throwable) {
        if (throwable instanceof BadRequestException
                || throwable instanceof MethodArgumentNotValidException
                || throwable instanceof BindException
                || throwable instanceof MethodArgumentTypeMismatchException) {
            return HttpStatus.BAD_REQUEST.value();
        }

        if (throwable instanceof UnauthorizedException) {
            return HttpStatus.UNAUTHORIZED.value();
        }

        if (throwable instanceof AccessDeniedException) {
            return HttpStatus.FORBIDDEN.value();
        }

        if (throwable instanceof NotFoundException) {
            return HttpStatus.NOT_FOUND.value();
        }

        if (throwable instanceof ConflictException) {
            return HttpStatus.CONFLICT.value();
        }

        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private static String resolveUserId(MethodSignature signature, Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Authentication authentication) {
                String userId = resolveUserId(authentication);
                if (userId != null) {
                    return userId;
                }
            }

            if (arg instanceof UserDetailsImpl userDetails) {
                return String.valueOf(userDetails.getId());
            }
        }

        String[] parameterNames = signature.getParameterNames();
        if (parameterNames == null) {
            return currentUserId();
        }

        for (int index = 0; index < parameterNames.length; index++) {
            if ("userId".equals(parameterNames[index]) && args[index] instanceof Long userId) {
                return String.valueOf(userId);
            }
        }

        return currentUserId();
    }

    private static String resolveUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return String.valueOf(userDetails.getId());
        }
        return null;
    }

    public static final class MdcScope implements AutoCloseable {

        private final String key;
        private final String previousValue;

        private MdcScope(String key, String value) {
            this.key = key;
            this.previousValue = MDC.get(key);
            if (value == null) {
                MDC.remove(key);
            } else {
                MDC.put(key, value);
            }
        }

        @Override
        public void close() {
            if (previousValue == null) {
                MDC.remove(key);
            } else {
                MDC.put(key, previousValue);
            }
        }
    }
}
