package kinderland.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import kinderland.order.model.entity.Order;
import kinderland.order.model.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByAccountEmailOrderByCreatedAtDesc(String accountEmail);

    /** Tất cả đơn, mới nhất trước (ADMIN/MANAGER xem trang quản trị). */
    List<Order> findAllByOrderByCreatedAtDesc();

    /** Đơn theo cửa hàng, mới nhất trước (manager xem đơn của store mình). */
    List<Order> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    /** Đơn ở trạng thái cho trước và được tạo TRƯỚC mốc thời gian (dùng để tìm đơn PENDING quá hạn). */
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime cutoff);

    /** Tổng doanh thu (đơn ở trạng thái cho trước, thường COMPLETED) — dùng cho Financial. */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal getTotalRevenue(@Param("status") OrderStatus status);

    /** Doanh thu trong khoảng thời gian (theo createdAt) — dùng cho Financial. */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.status = :status AND o.createdAt BETWEEN :start AND :end")
    BigDecimal getRevenueByDateRange(@Param("status") OrderStatus status,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);
}
