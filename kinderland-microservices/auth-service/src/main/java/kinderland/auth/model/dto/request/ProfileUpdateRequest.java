package kinderland.auth.model.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileUpdateRequest {
    @Schema(example = "null")
    private String phone;
    @Schema(example = "null")
    private String firstName;
    @Schema(example = "null")
    private String lastName;
}
