package kinderland.order.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    /** Alias khớp FE orderTypes.Order (đọc orderId/orderStatus/customer/shippingAddress/shippingCode). */
    private Long orderId;
    private String orderStatus;
    private CustomerDTO customer;
    private String shippingAddress;
    private String shippingCode;
    private String accountEmail;
    private Long storeId;
    private Long addressId;
    private BigDecimal totalAmount;
    /** Điểm loyalty đã dùng + số VND được giảm tương ứng (0 nếu không dùng). */
    private Integer pointsUsed;
    private BigDecimal pointsDiscount;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
