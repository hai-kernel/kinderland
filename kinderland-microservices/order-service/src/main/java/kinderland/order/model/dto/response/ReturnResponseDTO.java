package kinderland.order.model.dto.response;

import kinderland.order.model.entity.ReturnStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Khớp FE returnApi.ReturnResponseDTO. */
@Data
@Builder
public class ReturnResponseDTO {
    private Long returnId;
    private String returnCode;
    private Long orderItemId;
    private Long orderId;
    private String returnReason;
    private String rejectionReason;
    private ReturnStatus returnStatus;
    private String description;
    private List<String> photoUrls;
    private BigDecimal refundAmount;
    private String refundType;

    // Thông tin ngân hàng khách
    private String bankAccountNumber;
    private String bankName;
    private String bankAccountName;

    // Bằng chứng hoàn tiền
    private String refundTransactionCode;

    // Mốc thời gian
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private LocalDateTime refundedAt;

    // Thông tin khách (order-service chỉ giữ email; name/phone thuộc auth-service → null)
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Thông tin sản phẩm (snapshot trên OrderItem)
    private String productName;
    private Integer quantity;

    // Thông tin cửa hàng (enrich qua Feign product-service)
    private String storeName;
    private String storeAddress;
    private String storePhone;

    private String processedByName;
}
