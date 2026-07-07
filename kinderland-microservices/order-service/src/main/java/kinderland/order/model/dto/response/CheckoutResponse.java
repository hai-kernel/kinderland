package kinderland.order.model.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Kết quả checkout trả cho client.
 *  - VNPAY: paymentUrl != null -> client redirect sang cổng VNPay. Đơn GIỮ PENDING tới khi thanh toán xong.
 *  - COD:   paymentUrl == null -> đã ghi nhận, đơn sẽ chuyển PAID qua event.
 */
@Data
@Builder
public class CheckoutResponse {
    private Long orderId;
    private String paymentMethod;
    private String paymentStatus;
    private String paymentUrl;
    private String message;
}
