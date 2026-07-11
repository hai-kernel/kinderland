package kinderland.order.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Body xử lý hoàn tiền (khớp FE returnApi.RefundPayload). */
@Data
public class RefundRequestDTO {

    /** BANK_TRANSFER | E_GIFT. */
    @NotBlank(message = "Refund type is required")
    private String refundType;

    /** Mã giao dịch chuyển khoản (bằng chứng hoàn tiền, với BANK_TRANSFER). */
    private String bankTransactionCode;
}
