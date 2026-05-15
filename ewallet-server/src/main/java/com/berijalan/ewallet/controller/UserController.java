package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.security.CurrentUser;
import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.dto.request.ReqChangePasswordDto;
import com.berijalan.ewallet.dto.request.ReqEmailChangeConfirmDto;
import com.berijalan.ewallet.dto.request.ReqEmailChangeDto;
import com.berijalan.ewallet.dto.request.ReqUpdateProfileDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResEmailChangeDto;
import com.berijalan.ewallet.dto.response.ResUserSummaryDto;
import com.berijalan.ewallet.service.UserEmailChangeService;
import com.berijalan.ewallet.service.UserService;
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
public class UserController {

    private final UserService userService;
    private final UserEmailChangeService userEmailChangeService;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<ResUserSummaryDto>> getProfile(Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResUserSummaryDto data = userService.getProfile(userId);

        return ResponseEntity.ok(ApiResponseFactory.success("Profile retrieved successfully", data));
    }

    @PutMapping("/me")
    public ResponseEntity<BaseResponse<ResUserSummaryDto>> updateProfile(
            @Valid @RequestBody ReqUpdateProfileDto request,
            Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResUserSummaryDto data = userService.updateProfile(userId, request);

        return ResponseEntity.ok(ApiResponseFactory.success("Profile updated successfully", data));
    }

    @PostMapping("/me/email")
    public ResponseEntity<BaseResponse<ResEmailChangeDto>> requestEmailChange(
            @Valid @RequestBody ReqEmailChangeDto request,
            Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResEmailChangeDto data = userEmailChangeService.requestEmailChange(userId, request.newEmail());

        return ResponseEntity.ok(ApiResponseFactory.success("Verification token has been sent to " + request.newEmail(), data));
    }

    @PostMapping("/me/email/confirm")
    public ResponseEntity<BaseResponse<ResUserSummaryDto>> confirmEmailChange(
            @Valid @RequestBody ReqEmailChangeConfirmDto request,
            Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResUserSummaryDto data = userEmailChangeService.confirmEmailChange(userId, request.token());

        return ResponseEntity.ok(ApiResponseFactory.success("Email updated successfully", data));
    }

    @PutMapping("/me/password")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @Valid @RequestBody ReqChangePasswordDto request,
            Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        userService.changePassword(userId, request);

        return ResponseEntity.ok(ApiResponseFactory.success("Password changed successfully"));
    }
}
