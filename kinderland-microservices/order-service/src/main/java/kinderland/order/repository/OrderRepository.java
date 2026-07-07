package kinderland.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import kinderland.order.model.entity.Order;
import kinderland.order.model.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByAccountEmailOrderByCreatedAtDesc(String accountEmail);

    /** Đơn ở trạng thái cho trước và được tạo TRƯỚC mốc thời gian (dùng để tìm đơn PENDING quá hạn). */
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime cutoff);
}
