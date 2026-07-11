package kinderland.product.repository;

import kinderland.product.model.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    boolean existsByCode(String code);

    @Query("SELECT p FROM Promotion p WHERE " +
            ":keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Promotion> searchByMultipleFields(@Param("keyword") String keyword, Pageable pageable);
}
