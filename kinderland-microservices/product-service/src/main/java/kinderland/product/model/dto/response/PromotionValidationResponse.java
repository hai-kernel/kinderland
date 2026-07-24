package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Kết quả kiểm tra 1 mã khuyến mãi trên một subtotal cụ thể.
 *
 * Dùng chung cho CẢ hai đường:
 *  - Public  GET /api/v1/promotions/validate  → FE hiển thị số tiền giảm trước khi đặt hàng.
 *  - Internal GET /internal/promotions/validate → order-service TÍNH LẠI khi tạo đơn.
 *
 * Một nguồn sự thật duy nhất cho công thức giảm giá: số FE hiển thị và số backend lưu
 * luôn được sinh ra từ cùng một đoạn code, nên không thể lệch nhau.
 */
@Data
@Builder
public class PromotionValidationResponse {

    private boolean valid;

    /** Lý do không hợp lệ (hiển thị thẳng cho người dùng). null khi valid = true. */
    private String message;

    private Long promotionId;
    private String code;
    private String title;
    private BigDecimal discountPercent;

    /** Subtotal đã dùng để tính (echo lại input, tiện debug/log). */
    private BigDecimal subtotal;

    /** Số tiền được giảm, đã làm tròn và đã kẹp trần maxDiscountAmount + không vượt subtotal. */
    private BigDecimal discountAmount;
}
