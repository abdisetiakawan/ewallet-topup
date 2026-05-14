package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.dto.request.ReqLoginDto;
import com.berijalan.ewallet.dto.request.ReqRegisterDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResLoginDto;
import com.berijalan.ewallet.dto.response.ResRefreshTokenDto;
import com.berijalan.ewallet.dto.response.ResUserSummaryDto;
import com.berijalan.ewallet.exception.UnauthorizedException;
import com.berijalan.ewallet.security.UserDetailsImpl;
import com.berijalan.ewallet.service.AuthService;
import com.berijalan.ewallet.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

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
        return ResponseEntity.ok(ApiResponseFactory.success("Registration successful", data));
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<ResLoginDto>> login(@Valid @RequestBody ReqLoginDto request) {
        AuthService.LoginResult result = authService.login(request);

        ResponseCookie cookie = buildRefreshTokenCookie(result.refreshToken(), refreshTokenTtlDays);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponseFactory.success("Login successful", result.loginDto()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<ResRefreshTokenDto>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is missing");
        }

        AuthService.RefreshResult result = authService.refresh(refreshToken);
        ResRefreshTokenDto data = new ResRefreshTokenDto(result.accessToken(), "Bearer", result.expiresIn());

        return ResponseEntity.ok(ApiResponseFactory.success("Token refreshed", data));
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

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponseFactory.success("Logout successful"));
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
