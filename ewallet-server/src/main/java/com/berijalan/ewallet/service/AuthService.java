package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.request.ReqLoginDto;
import com.berijalan.ewallet.dto.request.ReqRegisterDto;
import com.berijalan.ewallet.dto.response.ResLoginDto;
import com.berijalan.ewallet.dto.response.ResUserSummaryDto;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.entity.Wallet;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.exception.UnauthorizedException;
import com.berijalan.ewallet.mapper.UserMapper;
import com.berijalan.ewallet.repository.UserRepository;
import com.berijalan.ewallet.repository.WalletRepository;
import com.berijalan.ewallet.security.JwtUtils;
import com.berijalan.ewallet.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    /**
     * Mendaftarkan customer baru dan membuat wallet awal dalam transaksi yang sama.
     *
     * @param request data registrasi customer.
     * @return ringkasan customer yang berhasil dibuat.
     * @throws BadRequestException jika email sudah digunakan.
     */
    @Transactional
    public ResUserSummaryDto register(ReqRegisterDto request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);

        // WHY: Setiap customer harus langsung memiliki wallet agar endpoint finansial tidak perlu membuatnya lazily.
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(0L);

        walletRepository.save(wallet);

        return userMapper.toSummaryDto(user);
    }

    /**
     * Mengautentikasi kredensial dan membuat access token serta refresh token sesi.
     *
     * @param request email dan password pengguna.
     * @return access token untuk response API dan refresh token untuk cookie.
     */
    public LoginResult login(ReqLoginDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // WHY: RefreshTokenService menerapkan single-session policy sebelum token baru diterbitkan.
        String refreshToken = refreshTokenService.create(userDetails.getId());

        ResLoginDto loginDto = userMapper.toLoginDto(accessToken, jwtUtils.getAccessTokenTtlSeconds(), userDetails);

        return new LoginResult(loginDto, refreshToken);
    }

    /**
     * Menerbitkan access token baru dari refresh token yang masih aktif.
     *
     * @param refreshToken token dari cookie HTTP-only.
     * @return access token baru beserta masa berlakunya.
     * @throws UnauthorizedException jika refresh token tidak valid atau kedaluwarsa.
     */
    public RefreshResult refresh(String refreshToken) {
        Long userId = refreshTokenService.validateAndGetUserId(refreshToken);
        if (userId == null) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtUtils.generateTokenForUserId(userId);
        return new RefreshResult(newAccessToken, jwtUtils.getAccessTokenTtlSeconds());
    }

    /**
     * Mencabut refresh token aktif milik user.
     *
     * @param userId ID user yang sedang logout.
     */
    public void logout(Long userId) {
        refreshTokenService.revoke(userId);
    }

    public record LoginResult(ResLoginDto loginDto, String refreshToken) {}

    public record RefreshResult(String accessToken, long expiresIn) {}
}
