package kinderland.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import kinderland.product.model.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
}
