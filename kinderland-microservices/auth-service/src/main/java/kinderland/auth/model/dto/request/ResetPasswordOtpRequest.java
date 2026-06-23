package kinderland.auth.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordOtpRequest {
    private String email;
    private String otp;
    private String newPassword;
}
