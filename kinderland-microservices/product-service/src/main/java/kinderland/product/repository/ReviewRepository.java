package kinderland.product.repository;

import kinderland.product.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findBySku_IdOrderByCreatedAtDesc(Long skuId);

    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    boolean existsByAccountEmailAndSku_Id(String accountEmail, Long skuId);
}
