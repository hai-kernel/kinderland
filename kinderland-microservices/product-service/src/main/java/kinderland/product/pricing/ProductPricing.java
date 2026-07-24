package kinderland.product.pricing;

import kinderland.product.model.entity.Promotion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * NGUỒN SỰ THẬT DUY NHẤT cho giá sau khuyến mãi Ở CẤP SẢN PHẨM (Promotion gắn trên Product).
 *
 * Trước đây phép tính "giá gốc - giảm %" chỉ nằm rải rác ở frontend (ProductDetail.tsx) nên
 * khuyến mãi chỉ hiển thị mà không đi vào giỏ/đơn/thanh toán. Gom về một chỗ tại backend để
 * mọi tầng (Internal SKU API → giỏ hàng → tạo đơn → thanh toán) dùng chung đúng một con số.
 *
 * Cùng công thức làm tròn với FE (originalPrice * pct / 100, làm tròn tới đồng) để giá hiển
 * thị trên trang chi tiết và giá chốt trong đơn khớp nhau tuyệt đối.
 */
public final class ProductPricing {

    private ProductPricing() {
    }

    /** Số tiền được giảm trên MỘT đơn vị. 0 nếu không có promotion hợp lệ. */
    public static BigDecimal discountAmount(BigDecimal originalPrice, Promotion promotion) {
        if (originalPrice == null || originalPrice.signum() <= 0 || !isActive(promotion)) {
            return BigDecimal.ZERO;
        }
        BigDecimal percent = promotion.getDiscountPercent();
        if (percent == null || percent.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = originalPrice.multiply(percent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        // Không bao giờ giảm quá giá gốc (percent bị nhập > 100 chẳng hạn).
        return discount.min(originalPrice).max(BigDecimal.ZERO);
    }

    /** Giá sau giảm của MỘT đơn vị. Bằng giá gốc khi không có promotion hợp lệ. */
    public static BigDecimal effectivePrice(BigDecimal originalPrice, Promotion promotion) {
        if (originalPrice == null) {
            return BigDecimal.ZERO;
        }
        return originalPrice.subtract(discountAmount(originalPrice, promotion)).max(BigDecimal.ZERO);
    }

    /**
     * Promotion có đang thực sự áp dụng được không: phải bật (active) VÀ thời điểm hiện tại
     * nằm trong khoảng [startDate, endDate]. Đây chính là điều kiện đã dùng ở luồng mã giảm
     * giá (PromotionService.validate) — giữ nhất quán để không có nơi nào "lọt" mã hết hạn.
     */
    public static boolean isActive(Promotion promotion) {
        if (promotion == null || !promotion.isActive()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (promotion.getStartDate() != null && now.isBefore(promotion.getStartDate())) {
            return false;
        }
        return promotion.getEndDate() == null || !now.isAfter(promotion.getEndDate());
    }
}
