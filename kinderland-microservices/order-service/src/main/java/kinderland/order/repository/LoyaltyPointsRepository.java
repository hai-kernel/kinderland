package kinderland.order.repository;

import kinderland.order.model.entity.LoyaltyPoints;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoyaltyPointsRepository extends JpaRepository<LoyaltyPoints, Long> {
    Optional<LoyaltyPoints> findByAccountEmail(String accountEmail);
}
