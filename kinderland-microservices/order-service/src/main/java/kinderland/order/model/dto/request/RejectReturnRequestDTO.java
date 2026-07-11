package kinderland.order.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectReturnRequestDTO {

    @NotBlank(message = "Rejection reason is required")
    private String rejectionReason;
}
