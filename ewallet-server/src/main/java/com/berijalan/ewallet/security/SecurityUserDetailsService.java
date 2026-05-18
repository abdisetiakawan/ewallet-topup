package com.berijalan.ewallet.security;

import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SecurityUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Memuat user untuk autentikasi login berbasis email.
     *
     * @param email email login.
     * @return principal Spring Security.
     * @throws UsernameNotFoundException jika email tidak terdaftar.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));

        return UserDetailsImpl.build(user);
    }

    /**
     * Memuat user untuk request JWT setelah subject userId dibaca dari token.
     *
     * @param id ID user dari subject JWT.
     * @return principal Spring Security.
     * @throws UsernameNotFoundException jika user tidak ditemukan.
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserDetailsById(Long id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with id: " + id));

        return UserDetailsImpl.build(user);
    }
}
