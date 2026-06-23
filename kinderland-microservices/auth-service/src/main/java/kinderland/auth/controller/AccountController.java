package kinderland.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import kinderland.auth.model.dto.request.ChangePasswordRequest;
import kinderland.auth.model.dto.request.ProfileUpdateRequest;
import kinderland.auth.model.dto.response.UserResponse;
import kinderland.auth.service.IProfileService;
import kinderland.common.dto.BaseResponse;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {
    private final IProfileService profileService;

    @PostMapping("/update-profile")
    public ResponseEntity<BaseResponse<Void>> updateProfile(
            @Validated @RequestBody ProfileUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        profileService.updateProfile(request);
        return ResponseEntity.ok(
                BaseResponse.ok(HttpStatus.OK.value(), httpRequest.getRequestURI(), "Profile updated", null)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserResponse>> getMyProfile(HttpServletRequest httpRequest) {
        UserResponse response = profileService.getProfile();
        return ResponseEntity.ok(
                BaseResponse.ok(HttpStatus.OK.value(), httpRequest.getRequestURI(), "Get profile successfully", response)
        );
    }

    @PostMapping("change-password")
    public ResponseEntity<BaseResponse<?>> changePassword(@RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        profileService.changePassword(request);
        return ResponseEntity.ok(
                BaseResponse.ok(HttpStatus.OK.value(), httpRequest.getRequestURI(), "Password changed successfully", null)
        );
    }
}
