package kinderland.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import kinderland.order.model.entity.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByAccountEmail(String accountEmail);
}
