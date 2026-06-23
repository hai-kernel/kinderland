package kinderland.order.client.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Bản sao payload Internal API của Product Service (deserialize từ JSON Feign trả về).
 * Cố ý KHÔNG share class với product-service để tránh coupling biên dịch giữa 2 service.
 */
@Data
public class ProductInternalResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private boolean active;
}
