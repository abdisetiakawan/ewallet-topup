package com.berijalan.ewallet.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingAspect {

    @Around("execution(public * com.berijalan.ewallet.controller..*(..))")
    public Object logControllerInvocation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startNanos = System.nanoTime();
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String handler = handlerName(joinPoint);

        try (LoggingSupport.MdcScope ignored = LoggingSupport.withUserId()) {
            try {
                Object result = joinPoint.proceed();
                int status = extractHttpStatus(result);
                logAtLevel(logger, LogLevel.INFO,
                        "HTTP {} {} handled by {} status={} userId={} durationMs={}",
                        mdcOrUnknown(LoggingSupport.HTTP_METHOD),
                        mdcOrUnknown(LoggingSupport.REQUEST_PATH),
                        handler,
                        status,
                        LoggingSupport.loggedUserId(),
                        elapsedMillis(startNanos));
                return result;
            } catch (Throwable ex) {
                int status = LoggingSupport.resolveHttpStatus(ex);
                LogLevel level = LoggingSupport.isExpectedException(ex) ? LogLevel.WARN : LogLevel.ERROR;
                logException(logger, level,
                        "HTTP {} {} handled by {} status={} userId={} durationMs={} error={}",
                        ex,
                        mdcOrUnknown(LoggingSupport.HTTP_METHOD),
                        mdcOrUnknown(LoggingSupport.REQUEST_PATH),
                        handler,
                        status,
                        LoggingSupport.loggedUserId(),
                        elapsedMillis(startNanos),
                        ex.getClass().getSimpleName());
                throw ex;
            }
        }
    }

    @Around("@annotation(com.berijalan.ewallet.logging.LoggableAction)")
    public Object logAnnotatedService(ProceedingJoinPoint joinPoint) throws Throwable {
        long startNanos = System.nanoTime();
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String method = handlerName(joinPoint);
        LoggableAction loggableAction = signature.getMethod().getAnnotation(LoggableAction.class);

        try (LoggingSupport.MdcScope ignored = LoggingSupport.withUserId(signature, joinPoint.getArgs())) {
            try {
                Object result = joinPoint.proceed();
                if (loggableAction.logSuccess()) {
                    logAtLevel(logger, loggableAction.level(),
                            "Action '{}' completed at {} userId={} durationMs={}",
                            loggableAction.action(),
                            method,
                            LoggingSupport.loggedUserId(),
                            elapsedMillis(startNanos));
                }
                return result;
            } catch (Throwable ex) {
                LogLevel level = LoggingSupport.isExpectedException(ex) ? LogLevel.WARN : LogLevel.ERROR;
                logException(logger, level,
                        "Action '{}' failed at {} userId={} durationMs={} error={}",
                        ex,
                        loggableAction.action(),
                        method,
                        LoggingSupport.loggedUserId(),
                        elapsedMillis(startNanos),
                        ex.getClass().getSimpleName());
                throw ex;
            }
        }
    }

    private String handlerName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }

    private int extractHttpStatus(Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getStatusCode().value();
        }
        return 200;
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private String mdcOrUnknown(String key) {
        String value = org.slf4j.MDC.get(key);
        return value != null ? value : "unknown";
    }

    private void logAtLevel(Logger logger, LogLevel level, String message, Object... args) {
        switch (level) {
            case DEBUG -> logger.debug(message, args);
            case INFO -> logger.info(message, args);
            case WARN -> logger.warn(message, args);
            case ERROR -> logger.error(message, args);
        }
    }

    private void logException(Logger logger, LogLevel level, String message, Throwable ex, Object... args) {
        if (LoggingSupport.isExpectedException(ex)) {
            logAtLevel(logger, level, message + " message={}", appendArg(args, ex.getMessage()));
            return;
        }

        Object[] stackTraceArgs = appendArg(args, ex);
        switch (level) {
            case DEBUG -> logger.debug(message, stackTraceArgs);
            case INFO -> logger.info(message, stackTraceArgs);
            case WARN -> logger.warn(message, stackTraceArgs);
            case ERROR -> logger.error(message, stackTraceArgs);
        }
    }

    private Object[] appendArg(Object[] args, Object extraArg) {
        Object[] combined = new Object[args.length + 1];
        System.arraycopy(args, 0, combined, 0, args.length);
        combined[args.length] = extraArg;
        return combined;
    }
}
