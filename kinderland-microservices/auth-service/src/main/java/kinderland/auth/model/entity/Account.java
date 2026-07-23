package kinderland.auth.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(unique = true)
    private String phone;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    private boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    private Role role;

    private String resetOtp;
    private LocalDateTime resetOtpExpiry;

    private String password;

    /**
     * Phương thức đăng nhập gốc của tài khoản.
     * GOOGLE = tài khoản do loginWithGoogle tạo ra, password chỉ là chuỗi ngẫu
     * nhiên không ai biết → không được phép đổi mật khẩu / bật 2FA cục bộ.
     * Cột thêm sau nên hàng cũ có giá trị NULL: coi NULL là LOCAL (xem
     * {@link #isPasswordLoginEnabled()}).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    private AuthProvider authProvider;

    /** Tài khoản có mật khẩu cục bộ dùng được hay không. */
    public boolean isPasswordLoginEnabled() {
        return authProvider != AuthProvider.GOOGLE;
    }

    public enum Role {
        ADMIN,
        MANAGER,
        CUSTOMER
    }

    public enum AuthProvider {
        LOCAL,
        GOOGLE
    }
}
