package kinderland.order.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Khớp FE loyaltyApi.LoyaltyPoints. Thêm trường mới KHÔNG phá client cũ. */
@Data
@Builder
public class LoyaltyPointsResponse {
    private Integer totalPoints;
    private Integer lifetimePoints;

    /**
     * Thời điểm số điểm hiện tại hết hạn = lastEarnedAt + 1 năm (xem
     * LoyaltyService.checkExpiration). NULL khi khách chưa tích điểm lần nào —
     * chưa có gì để hết hạn. Trước đây BE không trả trường này nên trang
     * "Điểm tích lũy" tự bịa ra "200 điểm hết hạn 2026-03-31" cho mọi tài khoản.
     */
    private LocalDateTime expiresAt;
}
