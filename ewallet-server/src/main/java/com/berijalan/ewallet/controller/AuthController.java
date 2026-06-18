package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.contract.api.AutentikasiApi;
import com.berijalan.ewallet.contract.model.BaseResponseResLoginDto;
import com.berijalan.ewallet.contract.model.BaseResponseResRefreshTokenDto;
import com.berijalan.ewallet.contract.model.BaseResponseResUserSummaryDto;
import com.berijalan.ewallet.contract.model.BaseResponseVoid;
import com.berijalan.ewallet.contract.model.ReqLoginDto;
import com.berijalan.ewallet.contract.model.ReqRegisterDto;
import com.berijalan.ewallet.contract.model.ResRefreshTokenDto;
import com.berijalan.ewallet.contract.model.ResUserSummaryDto;
import com.berijalan.ewallet.exception.UnauthorizedException;
import com.berijalan.ewallet.security.UserDetailsImpl;
import com.berijalan.ewallet.service.AuthService;
import com.berijalan.ewallet.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class AuthController implements AutentikasiApi {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.refresh-token.ttl-days:7}")
    private long refreshTokenTtlDays;

    /**
     * WHY: Cookie refresh token harus secure di produksi, tetapi development lokal biasanya belum memakai HTTPS.
     */
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Override
    public ResponseEntity<BaseResponseResUserSummaryDto> register(ReqRegisterDto request) {
        ResUserSummaryDto data = authService.register(request);
        return ResponseEntity.ok(ApiResponseFactory.success("Registration successful", data));
    }

    @Override
    public ResponseEntity<BaseResponseResLoginDto> login(ReqLoginDto request) {
        AuthService.LoginResult result = authService.login(request);

        ResponseCookie cookie = buildRefreshTokenCookie(result.refreshToken(), refreshTokenTtlDays);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponseFactory.success("Login successful", result.loginDto()));
    }

    @Override
    public ResponseEntity<BaseResponseResRefreshTokenDto> refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is missing");
        }

        AuthService.RefreshResult result = authService.refresh(refreshToken);
        ResRefreshTokenDto data = new ResRefreshTokenDto()
                .token(result.accessToken())
                .tokenType("Bearer")
                .expiresIn(result.expiresIn());

        return ResponseEntity.ok(ApiResponseFactory.success("Token refreshed", data));
    }

    @Override
    public ResponseEntity<BaseResponseVoid> logout(String refreshToken) {
        Long userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl user) {
            userId = user.getId();
        } else if (refreshToken != null && !refreshToken.isBlank()) {
            // WHY: Logout tetap harus bisa membersihkan sesi saat access token sudah kedaluwarsa.
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
