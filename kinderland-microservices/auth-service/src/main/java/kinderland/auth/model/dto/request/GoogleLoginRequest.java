package kinderland.auth.model.dto.request;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String idToken;
}
