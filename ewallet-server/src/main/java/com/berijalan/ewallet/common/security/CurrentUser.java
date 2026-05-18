package com.berijalan.ewallet.common.security;

import com.berijalan.ewallet.exception.UnauthorizedException;
import com.berijalan.ewallet.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long id(Authentication authentication) {
        return principal(authentication).getId();
    }

    /**
     * Mengambil principal aplikasi dari Spring Security context.
     *
     * @param authentication autentikasi request saat ini.
     * @return principal user aplikasi.
     * @throws UnauthorizedException jika request belum terautentikasi.
     */
    public static UserDetailsImpl principal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return principal;
    }
}
