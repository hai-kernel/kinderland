package kinderland.order.model.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Thông tin khách gắn trên OrderResponse (khớp FE orderTypes.CustomerDTO).
 * order-service chỉ biết email → fullName tạm để = email (tên thật thuộc auth-service).
 */
@Data
@Builder
public class CustomerDTO {
    private Long id;
    private String fullName;
    private String email;
}
