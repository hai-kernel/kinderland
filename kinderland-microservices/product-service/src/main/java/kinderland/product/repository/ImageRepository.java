package kinderland.product.repository;

import kinderland.product.model.entity.EntityType;
import kinderland.product.model.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    /** Lấy tất cả ảnh của một đối tượng (vd toàn bộ ảnh của 1 product). */
    List<Image> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);
}
