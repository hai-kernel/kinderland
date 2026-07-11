package kinderland.order.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** Body tạo yêu cầu trả hàng (khớp FE returnApi.ReturnRequestPayload). */
@Data
public class ReturnRequestDTO {

    @NotNull(message = "Order item ID cannot be null")
    private Long orderItemId;

    @NotBlank(message = "Return reason cannot be blank")
    private String returnReason;

    private String description;

    @NotEmpty(message = "Photo URLs cannot be empty")
    private List<String> photoUrls;

    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Bank account name is required")
    private String bankAccountName;
}
