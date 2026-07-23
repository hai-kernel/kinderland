package kinderland.auth.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import kinderland.auth.model.dto.request.ChangePasswordRequest;
import kinderland.auth.model.dto.request.ProfileUpdateRequest;
import kinderland.auth.model.dto.response.UserResponse;
import kinderland.auth.model.entity.Account;
import kinderland.auth.mapper.AccountMapper;
import kinderland.auth.repo.AccountRepository;
import kinderland.auth.service.IProfileService;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileServiceImpl implements IProfileService {
    AccountRepository accountRepository;
    PasswordEncoder passwordEncoder;
    AccountMapper accountMapper;

    @Override
    @Transactional
    public void updateProfile(ProfileUpdateRequest request) {
        Account account = currentAccount();

        if (StringUtils.hasText(request.getFirstName())) {
            account.setFirstName(request.getFirstName());
        }

        if (StringUtils.hasText(request.getLastName())) {
            account.setLastName(request.getLastName());
        }

        if (StringUtils.hasText(request.getPhone())) {
            if (!request.getPhone().equals(account.getPhone()) && accountRepository.existsByPhone(request.getPhone())) {
                throw new AppException(ErrorCode.PHONE_ALREADY_EXISTED);
            }
            account.setPhone(request.getPhone());
        }

        accountRepository.save(account);
    }

    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest request) {
        Account account = currentAccount();

        // Tài khoản Google có password là chuỗi ngẫu nhiên nội bộ — không ai biết
        // oldPassword nên trước đây chỉ trả WRONG_PASSWORD gây hiểu nhầm. Ẩn UI ở
        // FE là chưa đủ: chặn dứt điểm tại đây bằng lỗi nghiệp vụ rõ ràng.
        if (!account.isPasswordLoginEnabled()) {
            throw new AppException(ErrorCode.PASSWORD_LOGIN_NOT_ENABLED);
        }

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        if (passwordEncoder.matches(request.getNewPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATED);
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        account.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(account);
    }

    @Override
    public UserResponse getProfile() {
        return accountMapper.toUserResponse(currentAccount());
    }

    /** Lấy account hiện tại từ email mà Gateway đặt vào SecurityContext (qua HeaderAuthenticationFilter). */
    private Account currentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
}
