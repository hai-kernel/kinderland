package kinderland.payment.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kết quả khởi tạo thanh toán trả về order-service.
 *  - VNPAY: paymentUrl != null (client redirect sang cổng VNPay), status = PENDING.
 *  - COD:   paymentUrl == null, status = SUCCESS (đã ghi nhận, đơn sẽ PAID qua event).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitResponse {
    private Long orderId;
    private String method;
    private String status;
    private String paymentUrl;
}
