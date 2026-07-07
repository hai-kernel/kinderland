package kinderland.order.client.dto;

import lombok.Data;

/** Kết quả khởi tạo thanh toán do payment-service trả về. */
@Data
public class PaymentInitResponse {
    private Long orderId;
    private String method;
    private String status;
    private String paymentUrl;
}
