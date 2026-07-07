package kinderland.auth.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import kinderland.auth.event.UserCreatedEvent;
import kinderland.auth.event.UserEventPublisher;
import kinderland.auth.mapper.AccountMapper;
import kinderland.auth.model.dto.request.CreateAccountRequest;
import kinderland.auth.model.dto.request.RegisterRequest;
import kinderland.auth.model.dto.response.AuthResponse;
import kinderland.auth.model.dto.response.UserResponse;
import kinderland.auth.model.entity.Account;
import kinderland.auth.repo.AccountRepository;
import kinderland.auth.security.JwtUtil;
import kinderland.auth.service.AccountService;
import kinderland.auth.service.TokenBlacklistService;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountServiceImpl implements AccountService {
    AccountRepository accountRepository;
    PasswordEncoder passwordEncoder;
    AuthenticationManager authenticationManager;
    JwtUtil jwtUtil;
    TokenBlacklistService tokenBlacklistService;
    EmailService emailService;
    UserEventPublisher userEventPublisher;
    AccountMapper accountMapper;

    // @Value field: inject qua field nên KHÔNG được final -> @NonFinal để loại khỏi @RequiredArgsConstructor.
    @Value("${google.client-id}")
    @NonFinal
    String googleClientId;

    @Override
    public void registerAccount(RegisterRequest request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTED);
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTED);
        }
        if (accountRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTED);
        }

        Account account = Account.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .role(Account.Role.CUSTOMER)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        accountRepository.save(account);

        // Async: bắn event để notification-service gửi email chào mừng.
        publishUserCreated(account);
    }

    private void publishUserCreated(Account account) {
        UserCreatedEvent event = accountMapper.toUserCreatedEvent(account);
        event.setOccurredAt(Instant.now().toString());
        userEventPublisher.publishUserCreated(event);
    }

    @Override
    public AuthResponse authenticateAccount(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_CUSTOMER");

        String accessToken = jwtUtil.generateToken(userDetails.getUsername(), role);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername(), role);
        return new AuthResponse(accessToken, refreshToken, userDetails.getUsername(), role);
    }

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        String newAccessToken = jwtUtil.generateAccessTokenFromRefreshToken(refreshToken);
        String email = jwtUtil.getEmailFromToken(refreshToken);
        String role = jwtUtil.getRoleFromToken(refreshToken);

        return new AuthResponse(newAccessToken, refreshToken, email, role);
    }

    @Override
    public AuthResponse loginWithGoogle(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new AppException(ErrorCode.GOOGLE_ID_TOKEN_REQUIRED);
        }

        GoogleIdToken.Payload payload = verifyGoogleToken(idToken);
        String email = payload.getEmail();
        String firstName = (String) payload.getOrDefault("given_name", "Google");
        String lastName = (String) payload.getOrDefault("family_name", "User");

        Account account = accountRepository.findByEmail(email)
                .orElseGet(() -> createGoogleAccount(email, firstName, lastName));

        String role = "ROLE_" + account.getRole().name();
        String accessToken = jwtUtil.generateToken(account.getEmail(), role);
        String refreshToken = jwtUtil.generateRefreshToken(account.getEmail(), role);

        return new AuthResponse(accessToken, refreshToken, account.getEmail(), role);
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        // Ghi token vào Redis blacklist; Gateway sẽ đọc để chặn token này ngay lập tức.
        tokenBlacklistService.blacklist(token);
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            ).setAudience(Collections.singletonList(googleClientId)).build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new AppException(ErrorCode.INVALID_GOOGLE_ID_TOKEN);
            }
            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new AppException(ErrorCode.GOOGLE_TOKEN_VERIFICATION_FAILED, e);
        }
    }

    private Account createGoogleAccount(String email, String firstName, String lastName) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        Account account = Account.builder()
                .username(buildUniqueUsername(email, suffix))
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .role(Account.Role.CUSTOMER)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .build();

        Account saved = accountRepository.save(account);
        publishUserCreated(saved);
        return saved;
    }

    private String buildUniqueUsername(String email, String suffix) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9._-]", "");
        if (base.isBlank()) {
            base = "google_user";
        }

        String candidate = base;
        if (accountRepository.existsByUsername(candidate)) {
            candidate = base + "_" + suffix;
        }
        return candidate;
    }

    @Override
    public List<UserResponse> getAllAccounts() {
        return accountMapper.toUserResponseList(accountRepository.findAll());
    }

    @Override
    public UserResponse createAccountByAdmin(CreateAccountRequest request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTED);
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTED);
        }
        if (accountRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTED);
        }

        Account account = Account.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                // Nếu admin không truyền role, mặc định là CUSTOMER
                .role(request.getRole() != null ? request.getRole() : Account.Role.CUSTOMER)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        account = accountRepository.save(account);
        publishUserCreated(account);

        return accountMapper.toUserResponse(account);
    }

    @Override
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        accountRepository.delete(account);
    }

    @Override
    @Async
    public void forgetPassword(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

        String otp = generateOtp();

        account.setResetOtp(otp);
        account.setResetOtpExpiry(LocalDateTime.now().plusMinutes(15));

        accountRepository.save(account);

        emailService.sendOtpEmail(email, otp);
    }

    @Override
    public void resetPasswordWithOtp(String email, String otp, String newPassword) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (account.getResetOtp() == null || !account.getResetOtp().equals(otp)) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        if (account.getResetOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        account.setResetOtp(null);
        account.setResetOtpExpiry(null);

        accountRepository.save(account);
    }

    private String generateOtp() {
        int otp = (int) (Math.random() * 900000) + 100000; // 100000 → 999999
        return String.valueOf(otp);
    }
}
