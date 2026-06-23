package kinderland.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import kinderland.product.model.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
}
