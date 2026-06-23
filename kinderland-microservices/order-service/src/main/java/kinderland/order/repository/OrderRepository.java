package kinderland.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import kinderland.order.model.entity.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByAccountEmailOrderByCreatedAtDesc(String accountEmail);
}
