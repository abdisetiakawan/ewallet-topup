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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Autentikasi", description = "Registrasi, login, refresh token, dan logout pengguna.")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.refresh-token.ttl-days:7}")
    private long refreshTokenTtlDays;

    /**
     * WHY: Cookie refresh token harus secure di produksi, tetapi development lokal biasanya belum memakai HTTPS.
     */
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Operation(
            summary = "Registrasi pengguna",
            description = "Membuat akun customer baru dan wallet awal dengan saldo nol."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registrasi berhasil"),
            @ApiResponse(responseCode = "400", description = "Payload tidak valid atau email sudah terdaftar"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<ResUserSummaryDto>> register(@Valid @RequestBody ReqRegisterDto request) {
        ResUserSummaryDto data = authService.register(request);
        return ResponseEntity.ok(ApiResponseFactory.success("Registration successful", data));
    }

    @Operation(
            summary = "Login pengguna",
            description = "Menghasilkan access token JWT dan menyimpan refresh token di cookie HTTP-only."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login berhasil"),
            @ApiResponse(responseCode = "400", description = "Payload login tidak valid"),
            @ApiResponse(responseCode = "401", description = "Email atau password tidak valid"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<ResLoginDto>> login(@Valid @RequestBody ReqLoginDto request) {
        AuthService.LoginResult result = authService.login(request);

        ResponseCookie cookie = buildRefreshTokenCookie(result.refreshToken(), refreshTokenTtlDays);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponseFactory.success("Login successful", result.loginDto()));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Menerbitkan access token baru dari refresh token yang masih aktif di cookie."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token berhasil diperbarui"),
            @ApiResponse(responseCode = "401", description = "Refresh token tidak ada, tidak valid, atau kedaluwarsa"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<ResRefreshTokenDto>> refresh(
            @Parameter(description = "Refresh token HTTP-only yang dikirim melalui cookie login.")
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is missing");
        }

        AuthService.RefreshResult result = authService.refresh(refreshToken);
        ResRefreshTokenDto data = new ResRefreshTokenDto(result.accessToken(), "Bearer", result.expiresIn());

        return ResponseEntity.ok(ApiResponseFactory.success("Token refreshed", data));
    }

    @Operation(
            summary = "Logout pengguna",
            description = "Mencabut refresh token aktif dan menghapus cookie refresh token dari browser."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout berhasil"),
            @ApiResponse(responseCode = "401", description = "Refresh token tidak valid saat access token tidak tersedia"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user,
            @Parameter(description = "Refresh token opsional untuk logout saat access token sudah kedaluwarsa.")
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        Long userId = null;

        if (user != null) {
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
