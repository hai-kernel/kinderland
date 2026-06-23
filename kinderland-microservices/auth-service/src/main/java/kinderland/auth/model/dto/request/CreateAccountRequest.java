package kinderland.auth.model.dto.request;

import lombok.Data;
import kinderland.auth.model.entity.Account;

@Data
public class CreateAccountRequest {
    private String username;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String password;
    private Account.Role role;
}
