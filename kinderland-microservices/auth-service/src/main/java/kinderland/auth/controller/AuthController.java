package kinderland.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import kinderland.auth.model.dto.request.*;
import kinderland.auth.model.dto.response.AuthResponse;
import kinderland.auth.service.AccountService;
import kinderland.common.dto.BaseResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AuthResponse>> authenticateAccount(@RequestBody LoginRequest request,
                                                                          HttpServletRequest httpRequest) {
        AuthResponse token = accountService.authenticateAccount(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(
                BaseResponse.ok(HttpStatus.OK.value(), httpRequest.getRequestURI(), "Login successful", token)
        );
    }

    @PostMapping("/login/google")
    public ResponseEntity<BaseResponse<AuthResponse>> loginWithGoogle(@RequestBody GoogleLoginRequest request,
                                                                      HttpServletRequest httpRequest) {
        AuthResponse token = accountService.loginWithGoogle(request.getIdToken());
        return ResponseEntity.ok(
                BaseResponse.ok(HttpStatus.OK.value(), httpRequest.getRequestURI(), "Google login successful", token)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<AuthResponse>> refreshToken(@RequestBody RefreshTokenRequest request,
                                                                   HttpServletRequest httpRequest) {
        AuthResponse token = accountService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(
                BaseResponse.ok(HttpStatus.OK.value(), httpRequest.getRequestURI(), "Refresh token successful", token)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<Void>> registerAccount(@RequestBody RegisterRequest request,
                                                              HttpServletRequest httpRequest) {
        accountService.registerAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.ok(HttpStatus.CREATED.value(), httpRequest.getRequestURI(),
                        "Account registered successfully", null));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(
                    BaseResponse.error(HttpStatus.BAD_REQUEST.value(), request.getRequestURI(),
                            "Missing or invalid Authorization header", null)
            );
        }

        String token = authHeader.substring(7);
        accountService.logout(token);

        return ResponseEntity.ok(
                BaseResponse.ok(HttpStatus.OK.value(), request.getRequestURI(), "Logout successful", null)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<BaseResponse<?>> forgotPassword(@RequestBody ForgotPasswordRequest request,
                                                          HttpServletRequest httpRequest) {
        accountService.forgetPassword(request.getEmail());
        return ResponseEntity.ok(BaseResponse.ok(HttpStatus.OK.value(), httpRequest.getRequestURI(),
                "Password reset email sent", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<BaseResponse<?>> resetPasswordOtp(@RequestBody ResetPasswordOtpRequest request,
                                                            HttpServletRequest httpRequest) {
        accountService.resetPasswordWithOtp(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(BaseResponse.ok(HttpStatus.OK.value(), httpRequest.getRequestURI(),
                "Password reset completed", null));
    }
}
