package com.berijalan.ewallet.security;

import com.berijalan.ewallet.entity.constant.RoleName;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {

    private final JwtUtils jwtUtils = mock(JwtUtils.class);
    private final UserDetailsServiceImpl userDetailsService = mock(UserDetailsServiceImpl.class);
    private final JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtils, userDetailsService);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldBuildAuthenticationPrincipalFromToken() throws Exception {
        String token = "valid.jwt.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromJwtToken(token)).thenReturn("42");
        when(userDetailsService.loadUserById(42L))
                .thenReturn(new UserDetailsImpl(
                        42L,
                        "Admin",
                        "admin@example.com",
                        RoleName.ADMIN,
                        LocalDateTime.of(2026, 1, 1, 0, 0),
                        "password"
                ));

        jwtAuthFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(UserDetailsImpl.class);

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        assertThat(principal.getId()).isEqualTo(42L);
        assertThat(principal.getUsername()).isEqualTo("42");
        assertThat(principal.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");

        verify(filterChain).doFilter(request, response);
    }
}
