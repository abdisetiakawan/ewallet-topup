package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.security.CurrentUser;
import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.contract.api.PenggunaApi;
import com.berijalan.ewallet.contract.model.BaseResponseResEmailChangeDto;
import com.berijalan.ewallet.contract.model.BaseResponseResUserSummaryDto;
import com.berijalan.ewallet.contract.model.BaseResponseVoid;
import com.berijalan.ewallet.contract.model.ReqChangePasswordDto;
import com.berijalan.ewallet.contract.model.ReqEmailChangeConfirmDto;
import com.berijalan.ewallet.contract.model.ReqEmailChangeDto;
import com.berijalan.ewallet.contract.model.ReqUpdateProfileDto;
import com.berijalan.ewallet.contract.model.ResEmailChangeDto;
import com.berijalan.ewallet.contract.model.ResUserSummaryDto;
import com.berijalan.ewallet.service.UserEmailChangeService;
import com.berijalan.ewallet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements PenggunaApi {

    private final UserService userService;
    private final UserEmailChangeService userEmailChangeService;

    @Override
    public ResponseEntity<BaseResponseResUserSummaryDto> getProfile() {
        Long userId = currentUserId();
        ResUserSummaryDto data = userService.getProfile(userId);

        return ResponseEntity.ok(ApiResponseFactory.success("Profile retrieved successfully", data));
    }

    @Override
    public ResponseEntity<BaseResponseResUserSummaryDto> updateProfile(ReqUpdateProfileDto request) {
        Long userId = currentUserId();
        ResUserSummaryDto data = userService.updateProfile(userId, request);

        return ResponseEntity.ok(ApiResponseFactory.success("Profile updated successfully", data));
    }

    @Override
    public ResponseEntity<BaseResponseResEmailChangeDto> requestEmailChange(ReqEmailChangeDto request) {
        Long userId = currentUserId();
        ResEmailChangeDto data = userEmailChangeService.requestEmailChange(userId, request.getNewEmail());

        return ResponseEntity.ok(ApiResponseFactory.success("Verification token has been sent to " + request.getNewEmail(), data));
    }

    @Override
    public ResponseEntity<BaseResponseResUserSummaryDto> confirmEmailChange(ReqEmailChangeConfirmDto request) {
        Long userId = currentUserId();
        ResUserSummaryDto data = userEmailChangeService.confirmEmailChange(userId, request.getToken());

        return ResponseEntity.ok(ApiResponseFactory.success("Email updated successfully", data));
    }

    @Override
    public ResponseEntity<BaseResponseVoid> changePassword(ReqChangePasswordDto request) {
        Long userId = currentUserId();
        userService.changePassword(userId, request);

        return ResponseEntity.ok(ApiResponseFactory.success("Password changed successfully"));
    }

    private Long currentUserId() {
        return CurrentUser.id(SecurityContextHolder.getContext().getAuthentication());
    }
}
