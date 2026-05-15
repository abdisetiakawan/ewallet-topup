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
import org.springframework.security.core.AuthenticationException;
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

    @Transactional
    public ResUserSummaryDto register(ReqRegisterDto request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Registration rejected because email already exists. email={}", request.email());
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(0L);

        walletRepository.save(wallet);

        log.info("Registration success. userId={}, email={}", user.getId(), user.getEmail());

        return userMapper.toSummaryDto(user);
    }

    public LoginResult login(ReqLoginDto request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException ex) {
            log.error("Login failed during authentication. email={}", request.email(), ex);
            throw ex;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String refreshToken = refreshTokenService.create(userDetails.getId());

        ResLoginDto loginDto = userMapper.toLoginDto(accessToken, jwtUtils.getAccessTokenTtlSeconds(), userDetails);

        log.info("Login success. userId={}, email={}", userDetails.getId(), userDetails.getEmail());

        return new LoginResult(loginDto, refreshToken);
    }

    public RefreshResult refresh(String refreshToken) {
        Long userId = refreshTokenService.validateAndGetUserId(refreshToken);
        if (userId == null) {
            log.warn("Access token refresh rejected because refresh token is invalid or expired");
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtUtils.generateTokenForUserId(userId);
        log.info("Access token refresh success. userId={}", userId);
        return new RefreshResult(newAccessToken, jwtUtils.getAccessTokenTtlSeconds());
    }

    public void logout(Long userId) {
        refreshTokenService.revoke(userId);
        log.info("Logout success. userId={}", userId);
    }

    public record LoginResult(ResLoginDto loginDto, String refreshToken) {}

    public record RefreshResult(String accessToken, long expiresIn) {}
}
