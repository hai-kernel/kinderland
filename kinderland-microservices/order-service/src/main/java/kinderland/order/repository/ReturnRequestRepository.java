package kinderland.order.repository;

import kinderland.order.model.entity.ReturnRequest;
import kinderland.order.model.entity.ReturnStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    boolean existsByOrderItem_Id(Long orderItemId);

    Page<ReturnRequest> findByCustomerEmail(String customerEmail, Pageable pageable);

    /** Tổng tiền đã hoàn (các return REFUNDED) — dùng cho Financial. */
    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM ReturnRequest r WHERE r.returnStatus = :status")
    BigDecimal getTotalRefundAmount(@Param("status") ReturnStatus status);

    /** Tiền hoàn trong khoảng thời gian (theo refundedAt) — dùng cho Financial. */
    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM ReturnRequest r " +
            "WHERE r.returnStatus = :status AND r.refundedAt BETWEEN :start AND :end")
    BigDecimal getRefundAmountByDateRange(@Param("status") ReturnStatus status,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);
}
