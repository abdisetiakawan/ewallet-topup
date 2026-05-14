package com.berijalan.ewallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReqChangePasswordDto(
        @NotBlank(message = "Password lama wajib diisi")
        String oldPassword,

        @NotBlank(message = "Password baru wajib diisi")
        @Size(min = 8, message = "Password baru minimal 8 karakter")
        String newPassword
) {}
