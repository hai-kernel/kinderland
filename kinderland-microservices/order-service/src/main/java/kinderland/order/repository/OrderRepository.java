package kinderland.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import kinderland.order.model.entity.Order;
import kinderland.order.model.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByAccountEmailOrderByCreatedAtDesc(String accountEmail);

    /** Tất cả đơn, mới nhất trước (ADMIN/MANAGER xem trang quản trị). */
    List<Order> findAllByOrderByCreatedAtDesc();

    /** Đơn theo cửa hàng, mới nhất trước (manager xem đơn của store mình). */
    List<Order> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    /** Đơn ở trạng thái cho trước và được tạo TRƯỚC mốc thời gian (dùng để tìm đơn PENDING quá hạn). */
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime cutoff);

    /**
     * Tổng doanh thu — tính theo DANH SÁCH trạng thái, không phải một trạng thái duy nhất.
     *
     * Trước đây chỉ đếm COMPLETED, nhưng vòng đời là
     * PENDING -> PAID -> SHIPPING -> DELIVERED -> COMPLETED.
     * Đơn đã trả tiền nhưng chưa giao xong (PAID/SHIPPING/DELIVERED) vẫn là doanh thu đã thu,
     * nên chỉ đếm COMPLETED khiến dashboard hiển thị 0đ dù tiền đã về.
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status IN :statuses")
    BigDecimal getTotalRevenue(@Param("statuses") Collection<OrderStatus> statuses);

    /** Doanh thu trong khoảng thời gian (theo createdAt) — dùng cho Financial. */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.status IN :statuses AND o.createdAt BETWEEN :start AND :end")
    BigDecimal getRevenueByDateRange(@Param("statuses") Collection<OrderStatus> statuses,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);
}
