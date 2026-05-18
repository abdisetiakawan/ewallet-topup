package com.berijalan.ewallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload perubahan password customer.")
public record ReqChangePasswordDto(
        @Schema(description = "Password lama untuk verifikasi pemilik akun.", example = "OldPassword1")
        @NotBlank(message = "Password lama wajib diisi")
        String oldPassword,

        @Schema(description = "Password baru minimal 8 karakter.", example = "NewPassword1")
        @NotBlank(message = "Password baru wajib diisi")
        @Size(min = 8, message = "Password baru minimal 8 karakter")
        String newPassword
) {}
