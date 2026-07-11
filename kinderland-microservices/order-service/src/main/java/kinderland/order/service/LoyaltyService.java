package kinderland.order.service;

import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.order.model.dto.response.LoyaltyPointsResponse;
import kinderland.order.model.entity.LoyaltyPoints;
import kinderland.order.repository.LoyaltyPointsRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Điểm tích luỹ, port từ monolith LoyaltyServiceImpl nhưng keyed by accountEmail.
 * Tỉ lệ: 1 điểm / 1 VND chi trả; 1 điểm = 1 VND khi dùng (giữ đúng như monolith).
 * Điểm hết hạn sau 1 năm kể từ lần tích đầu tiên (checkExpiration).
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoyaltyService {

    static BigDecimal EARN_RATE = BigDecimal.valueOf(1);   // 1 điểm / 1 VND
    static BigDecimal SPEND_RATE = BigDecimal.valueOf(1);  // 1 điểm = 1 VND

    LoyaltyPointsRepository loyaltyPointsRepository;

    @Transactional
    public LoyaltyPointsResponse getMyPoints(String accountEmail) {
        LoyaltyPoints lp = getOrCreate(accountEmail);
        checkExpiration(lp);
        return LoyaltyPointsResponse.builder()
                .totalPoints(lp.getTotalPoints())
                .lifetimePoints(lp.getLifetimePoints())
                .build();
    }

    /** Tích điểm sau khi đơn hoàn tất. */
    @Transactional
    public void awardPoints(String accountEmail, BigDecimal amountPaid) {
        if (amountPaid == null) {
            return;
        }
        int pointsEarned = amountPaid.divide(EARN_RATE, 0, RoundingMode.FLOOR).intValue();
        if (pointsEarned <= 0) {
            return;
        }
        LoyaltyPoints lp = getOrCreate(accountEmail);
        checkExpiration(lp);
        lp.setTotalPoints(lp.getTotalPoints() + pointsEarned);
        lp.setLifetimePoints(lp.getLifetimePoints() + pointsEarned);
        if (lp.getLastEarnedAt() == null) {
            lp.setLastEarnedAt(LocalDateTime.now());
        }
        loyaltyPointsRepository.save(lp);
    }

    /** Trừ điểm lúc checkout, trả về số VND được giảm. */
    @Transactional
    public BigDecimal usePoints(String accountEmail, Integer pointsToUse) {
        if (pointsToUse == null || pointsToUse <= 0) {
            return BigDecimal.ZERO;
        }
        LoyaltyPoints lp = getOrCreate(accountEmail);
        checkExpiration(lp);
        if (lp.getTotalPoints() < pointsToUse) {
            throw new AppException(ErrorCode.INSUFFICIENT_POINTS);
        }
        lp.setTotalPoints(lp.getTotalPoints() - pointsToUse);
        loyaltyPointsRepository.save(lp);
        return BigDecimal.valueOf(pointsToUse).multiply(SPEND_RATE);
    }

    /** Trừ lại điểm đã tích khi đơn được hoàn tiền. */
    @Transactional
    public void deductPoints(String accountEmail, BigDecimal refundedAmount) {
        if (refundedAmount == null) {
            return;
        }
        int pointsToDeduct = refundedAmount.divide(EARN_RATE, 0, RoundingMode.FLOOR).intValue();
        if (pointsToDeduct <= 0) {
            return;
        }
        LoyaltyPoints lp = getOrCreate(accountEmail);
        lp.setTotalPoints(Math.max(lp.getTotalPoints() - pointsToDeduct, 0));
        loyaltyPointsRepository.save(lp);
    }

    /** Nếu quá 1 năm kể từ lần tích đầu, reset điểm về 0 và bắt đầu chu kỳ mới. */
    private void checkExpiration(LoyaltyPoints lp) {
        if (lp.getLastEarnedAt() != null
                && lp.getLastEarnedAt().isBefore(LocalDateTime.now().minusYears(1))) {
            lp.setTotalPoints(0);
            lp.setLastEarnedAt(null);
            loyaltyPointsRepository.save(lp);
        }
    }

    private LoyaltyPoints getOrCreate(String accountEmail) {
        return loyaltyPointsRepository.findByAccountEmail(accountEmail)
                .orElseGet(() -> loyaltyPointsRepository.save(LoyaltyPoints.builder()
                        .accountEmail(accountEmail)
                        .totalPoints(0)
                        .lifetimePoints(0)
                        .build()));
    }
}
