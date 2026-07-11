package kinderland.product.repository;

import kinderland.product.model.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsByName(String name);
}
