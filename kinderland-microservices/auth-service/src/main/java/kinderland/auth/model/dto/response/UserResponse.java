package kinderland.auth.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String role;
    /** "LOCAL" | "GOOGLE" — FE dùng để ẩn phần bảo mật mật khẩu với tài khoản Google. */
    private String authProvider;
    /** Tài khoản có mật khẩu cục bộ để đổi hay không (nguồn sự thật cho FE). */
    private boolean passwordLoginEnabled;
    private boolean active;
    private LocalDateTime createdAt;
}
