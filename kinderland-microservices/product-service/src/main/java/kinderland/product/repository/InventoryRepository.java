package kinderland.product.repository;

import kinderland.product.model.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findBySkuIdAndStoreId(Long skuId, Long storeId);
    List<Inventory> findByStoreId(Long storeId);
    List<Inventory> findBySkuId(Long skuId);
}
