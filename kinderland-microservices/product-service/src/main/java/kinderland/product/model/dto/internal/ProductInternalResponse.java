package kinderland.product.model.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload Internal API trả cho Order Service (qua Feign).
 * Chỉ chứa thông tin order cần để kiểm tra giá/tồn kho khi tạo đơn.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInternalResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private boolean active;
}
