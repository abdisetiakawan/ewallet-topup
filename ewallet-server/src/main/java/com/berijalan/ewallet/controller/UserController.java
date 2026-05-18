package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.security.CurrentUser;
import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.config.OpenApiConfig;
import com.berijalan.ewallet.dto.request.ReqChangePasswordDto;
import com.berijalan.ewallet.dto.request.ReqEmailChangeConfirmDto;
import com.berijalan.ewallet.dto.request.ReqEmailChangeDto;
import com.berijalan.ewallet.dto.request.ReqUpdateProfileDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResEmailChangeDto;
import com.berijalan.ewallet.dto.response.ResUserSummaryDto;
import com.berijalan.ewallet.service.UserEmailChangeService;
import com.berijalan.ewallet.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Pengguna", description = "Profil, perubahan email, dan perubahan password customer.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class UserController {

    private final UserService userService;
    private final UserEmailChangeService userEmailChangeService;

    @Operation(
            summary = "Melihat profil",
            description = "Mengambil ringkasan profil customer yang sedang login."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil berhasil dikembalikan"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna tidak diizinkan mengakses profil customer"),
            @ApiResponse(responseCode = "404", description = "Pengguna tidak ditemukan"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<ResUserSummaryDto>> getProfile(
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResUserSummaryDto data = userService.getProfile(userId);

        return ResponseEntity.ok(ApiResponseFactory.success("Profile retrieved successfully", data));
    }

    @Operation(
            summary = "Memperbarui profil",
            description = "Memperbarui data profil dasar customer yang sedang login."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil berhasil diperbarui"),
            @ApiResponse(responseCode = "400", description = "Payload profil tidak valid"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna tidak diizinkan mengubah profil customer"),
            @ApiResponse(responseCode = "404", description = "Pengguna tidak ditemukan"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @PutMapping("/me")
    public ResponseEntity<BaseResponse<ResUserSummaryDto>> updateProfile(
            @Valid @RequestBody ReqUpdateProfileDto request,
            @Parameter(hidden = true)
            Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResUserSummaryDto data = userService.updateProfile(userId, request);

        return ResponseEntity.ok(ApiResponseFactory.success("Profile updated successfully", data));
    }

    @Operation(
            summary = "Meminta perubahan email",
            description = "Mengirim token verifikasi ke email baru dan mencabut request email sebelumnya."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token verifikasi email berhasil dikirim"),
            @ApiResponse(responseCode = "400", description = "Payload email tidak valid"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna tidak diizinkan mengubah email customer"),
            @ApiResponse(responseCode = "409", description = "Email baru sudah digunakan"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @PostMapping("/me/email")
    public ResponseEntity<BaseResponse<ResEmailChangeDto>> requestEmailChange(
            @Valid @RequestBody ReqEmailChangeDto request,
            @Parameter(hidden = true)
            Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResEmailChangeDto data = userEmailChangeService.requestEmailChange(userId, request.newEmail());

        return ResponseEntity.ok(ApiResponseFactory.success("Verification token has been sent to " + request.newEmail(), data));
    }

    @Operation(
            summary = "Konfirmasi perubahan email",
            description = "Memvalidasi token email change dan mengganti email customer yang sedang login."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email berhasil diperbarui"),
            @ApiResponse(responseCode = "400", description = "Payload tidak valid atau token salah/kedaluwarsa"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna tidak diizinkan mengubah email customer"),
            @ApiResponse(responseCode = "404", description = "Pengguna tidak ditemukan"),
            @ApiResponse(responseCode = "409", description = "Email baru sudah digunakan"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @PostMapping("/me/email/confirm")
    public ResponseEntity<BaseResponse<ResUserSummaryDto>> confirmEmailChange(
            @Valid @RequestBody ReqEmailChangeConfirmDto request,
            @Parameter(hidden = true)
            Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResUserSummaryDto data = userEmailChangeService.confirmEmailChange(userId, request.token());

        return ResponseEntity.ok(ApiResponseFactory.success("Email updated successfully", data));
    }

    @Operation(
            summary = "Mengubah password",
            description = "Mengganti password customer setelah password lama berhasil diverifikasi."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password berhasil diubah"),
            @ApiResponse(responseCode = "400", description = "Payload tidak valid atau password lama salah"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna tidak diizinkan mengubah password customer"),
            @ApiResponse(responseCode = "404", description = "Pengguna tidak ditemukan"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @PutMapping("/me/password")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @Valid @RequestBody ReqChangePasswordDto request,
            @Parameter(hidden = true)
            Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        userService.changePassword(userId, request);

        return ResponseEntity.ok(ApiResponseFactory.success("Password changed successfully"));
    }
}
