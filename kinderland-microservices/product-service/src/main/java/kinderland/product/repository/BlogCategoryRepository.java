package kinderland.product.repository;

import kinderland.product.model.entity.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Long> {
    boolean existsByName(String name);
}
