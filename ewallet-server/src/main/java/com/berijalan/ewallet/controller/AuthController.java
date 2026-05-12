package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.config.MdcFilter;
import com.berijalan.ewallet.dto.request.ReqLoginDto;
import com.berijalan.ewallet.dto.request.ReqRegisterDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResLoginDto;
import com.berijalan.ewallet.dto.response.ResUserSummaryDto;
import com.berijalan.ewallet.exception.UnauthorizedException;
import com.berijalan.ewallet.security.UserDetailsImpl;
import com.berijalan.ewallet.service.AuthService;
import com.berijalan.ewallet.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.refresh-token.ttl-days:7}")
    private long refreshTokenTtlDays;

    /** Controls the Secure flag on the refresh token cookie. Set true in production (HTTPS). */
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<ResUserSummaryDto>> register(@Valid @RequestBody ReqRegisterDto request) {
        ResUserSummaryDto data = authService.register(request);
        BaseResponse<ResUserSummaryDto> response = new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Registration successful",
                data
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<ResLoginDto>> login(@Valid @RequestBody ReqLoginDto request) {
        AuthService.LoginResult result = authService.login(request);

        ResponseCookie cookie = buildRefreshTokenCookie(result.refreshToken(), refreshTokenTtlDays);

        BaseResponse<ResLoginDto> response = new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Login successful",
                result.loginDto()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<Map<String, Object>>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is missing");
        }

        AuthService.RefreshResult result = authService.refresh(refreshToken);

        Map<String, Object> data = Map.of(
                "token", result.accessToken(),
                "tokenType", "Bearer",
                "expiresIn", result.expiresIn()
        );

        BaseResponse<Map<String, Object>> response = new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Token refreshed",
                data
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            @AuthenticationPrincipal UserDetailsImpl user,
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        Long userId = null;

        if (user != null) {
            userId = user.getId();
        } else if (refreshToken != null && !refreshToken.isBlank()) {
            // Fallback: access token may be expired, extract userId from the refresh token in Redis
            userId = refreshTokenService.validateAndGetUserId(refreshToken);
        }

        if (userId != null) {
            authService.logout(userId);
        }

        ResponseCookie cookie = buildRefreshTokenCookie("", 0);

        BaseResponse<Void> response = new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Logout successful",
                null
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    private ResponseCookie buildRefreshTokenCookie(String value, long maxAgeDays) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(maxAgeDays > 0 ? Duration.ofDays(maxAgeDays) : Duration.ZERO)
                .build();
    }
}
