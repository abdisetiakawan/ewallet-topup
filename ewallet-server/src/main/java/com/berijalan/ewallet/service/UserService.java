package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.request.ReqChangePasswordDto;
import com.berijalan.ewallet.dto.request.ReqUpdateProfileDto;
import com.berijalan.ewallet.dto.response.ResUserSummaryDto;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.exception.NotFoundException;
import com.berijalan.ewallet.mapper.UserMapper;
import com.berijalan.ewallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * Mengambil profil customer yang sedang login.
     *
     * @param userId ID customer dari JWT.
     * @return ringkasan profil customer.
     * @throws NotFoundException jika user tidak ditemukan.
     */
    @Transactional(readOnly = true)
    public ResUserSummaryDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return userMapper.toSummaryDto(user);
    }

    /**
     * Memperbarui nama profil customer.
     *
     * @param userId ID customer dari JWT.
     * @param request data profil baru.
     * @return profil setelah diperbarui.
     * @throws NotFoundException jika user tidak ditemukan.
     */
    @Transactional
    public ResUserSummaryDto updateProfile(Long userId, ReqUpdateProfileDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setName(request.name());
        userRepository.save(user);

        return userMapper.toSummaryDto(user);
    }

    /**
     * Mengubah password customer setelah password lama berhasil diverifikasi.
     *
     * @param userId ID customer dari JWT.
     * @param request password lama dan password baru.
     * @throws BadRequestException jika password lama salah atau password baru sama dengan password lama.
     * @throws NotFoundException jika user tidak ditemukan.
     */
    @Transactional
    public void changePassword(Long userId, ReqChangePasswordDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password does not match");
        }

        if (request.oldPassword().equals(request.newPassword())) {
            throw new BadRequestException("New password cannot be the same as old password");
        }

        // WHY: Password hanya dibandingkan dalam bentuk plaintext request, lalu disimpan ulang dalam bentuk hash.
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for userId={}", userId);
    }
}
