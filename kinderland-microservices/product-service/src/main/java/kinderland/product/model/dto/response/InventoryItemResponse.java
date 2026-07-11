package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

/** 1 dòng tồn kho (GET /inventory). */
@Data
@Builder
public class InventoryItemResponse {
    private Long id;
    private Long skuId;
    private String skuCode;
    private String productName;
    private String color;
    private String size;
    private String type;
    private Integer quantity;
    private Long storeId;
    private String storeName;
}
