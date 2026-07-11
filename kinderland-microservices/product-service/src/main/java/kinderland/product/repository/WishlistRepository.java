package kinderland.product.repository;

import kinderland.product.model.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByAccountEmail(String accountEmail);
}
