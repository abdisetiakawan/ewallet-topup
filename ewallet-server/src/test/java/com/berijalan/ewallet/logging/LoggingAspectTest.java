package com.berijalan.ewallet.logging;

import com.berijalan.ewallet.entity.constant.RoleName;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.security.UserDetailsImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class LoggingAspectTest {

    private final LoggingAspect loggingAspect = new LoggingAspect();

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @AfterEach
    void tearDown() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldLogControllerSuccessWithRequestMetadata(CapturedOutput output) throws Throwable {
        authenticate(42L);
        putRequestContext("req-123", "POST", "/api/test");
        mockJoinPoint(new SampleController(), "create");
        when(joinPoint.proceed()).thenReturn(ResponseEntity.status(201).body("created"));

        Object result = loggingAspect.logControllerInvocation(joinPoint);

        assertThat(result).isInstanceOf(ResponseEntity.class);
        assertThat(output.getOut()).contains("HTTP POST /api/test handled by SampleController.create status=201 userId=42");
    }

    @Test
    void shouldLogAnonymousControllerRequestWhenNoAuthentication(CapturedOutput output) throws Throwable {
        putRequestContext("req-124", "POST", "/api/auth/login");
        mockJoinPoint(new SampleController(), "login");
        when(joinPoint.proceed()).thenReturn(ResponseEntity.ok("ok"));

        loggingAspect.logControllerInvocation(joinPoint);

        assertThat(output.getOut()).contains("HTTP POST /api/auth/login handled by SampleController.login status=200 userId=anonymous");
    }

    @Test
    void shouldLogWarnForExpectedControllerFailure(CapturedOutput output) throws Throwable {
        authenticate(7L);
        putRequestContext("req-125", "POST", "/api/wallet/topup");
        mockJoinPoint(new SampleController(), "topup");
        when(joinPoint.proceed()).thenThrow(new BadRequestException("bad request"));

        assertThatThrownBy(() -> loggingAspect.logControllerInvocation(joinPoint))
                .isInstanceOf(BadRequestException.class);

        assertThat(output.getOut())
                .contains("WARN")
                .contains("status=400 userId=7")
                .contains("error=BadRequestException")
                .contains("message=bad request")
                .doesNotContain("com.berijalan.ewallet.exception.BadRequestException: bad request");
    }

    @Test
    void shouldLogErrorForUnexpectedControllerFailure(CapturedOutput output) throws Throwable {
        authenticate(9L);
        putRequestContext("req-126", "GET", "/api/users/me");
        mockJoinPoint(new SampleController(), "getProfile");
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> loggingAspect.logControllerInvocation(joinPoint))
                .isInstanceOf(IllegalStateException.class);

        assertThat(output.getOut()).contains("ERROR").contains("status=500 userId=9").contains("error=IllegalStateException");
    }

    @Test
    void shouldLogAnnotatedServiceOnly(CapturedOutput output) {
        authenticate(55L);
        SampleService proxy = proxiedService();

        proxy.annotatedAction();
        proxy.plainAction();

        assertThat(output.getOut()).contains("Action 'sample.annotated' completed at SampleService.annotatedAction userId=55");
        assertThat(output.getOut()).doesNotContain("plainAction");
    }

    @Test
    void shouldSkipServiceSuccessLogWhenDisabled(CapturedOutput output) {
        SampleService proxy = proxiedService();

        proxy.annotatedWithoutSuccessLog(77L);

        assertThat(output.getOut()).doesNotContain("sample.no-success");
    }

    private void authenticate(Long userId) {
        UserDetailsImpl principal = new UserDetailsImpl(
                userId,
                "Test User",
                "test@example.com",
                RoleName.CUSTOMER,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                "password"
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    private void putRequestContext(String requestId, String method, String path) {
        MDC.put(LoggingSupport.REQUEST_ID, requestId);
        MDC.put(LoggingSupport.HTTP_METHOD, method);
        MDC.put(LoggingSupport.REQUEST_PATH, path);
    }

    private void mockJoinPoint(Object target, String methodName) {
        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getDeclaringType()).thenReturn(target.getClass());
        when(methodSignature.getName()).thenReturn(methodName);
    }

    private SampleService proxiedService() {
        AspectJProxyFactory factory = new AspectJProxyFactory(new SampleService());
        factory.addAspect(loggingAspect);
        return factory.getProxy();
    }

    static class SampleController {
        ResponseEntity<String> create() {
            return ResponseEntity.ok("created");
        }

        ResponseEntity<String> login() {
            return ResponseEntity.ok("ok");
        }

        ResponseEntity<String> topup() {
            return ResponseEntity.ok("ok");
        }

        ResponseEntity<String> getProfile() {
            return ResponseEntity.ok("ok");
        }
    }

    static class SampleService {

        @LoggableAction(action = "sample.annotated")
        List<String> annotatedAction() {
            return List.of("ok");
        }

        @LoggableAction(action = "sample.no-success", logSuccess = false)
        List<String> annotatedWithoutSuccessLog(Long userId) {
            return List.of("ok-" + userId);
        }

        List<String> plainAction() {
            return List.of("skip");
        }
    }
}
