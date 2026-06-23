package kinderland.auth.service;

import kinderland.auth.model.dto.request.CreateAccountRequest;
import kinderland.auth.model.dto.request.RegisterRequest;
import kinderland.auth.model.dto.response.AuthResponse;
import kinderland.auth.model.dto.response.UserResponse;

import java.util.List;

public interface AccountService {
    void registerAccount(RegisterRequest request);

    AuthResponse authenticateAccount(String username, String password);

    AuthResponse refreshAccessToken(String refreshToken);

    AuthResponse loginWithGoogle(String idToken);

    void logout(String token);

    List<UserResponse> getAllAccounts();

    UserResponse createAccountByAdmin(CreateAccountRequest request);

    void deleteAccount(Long id);

    void forgetPassword(String email);

    void resetPasswordWithOtp(String email, String otp, String newPassword);
}
