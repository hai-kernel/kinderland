package kinderland.product.repository;

import kinderland.product.model.entity.TransferOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferOrderRepository extends JpaRepository<TransferOrder, Long> {

    /** Phiếu chuyển kho liên quan tới 1 store (gửi đi hoặc nhận về). */
    List<TransferOrder> findByFromStore_IdOrToStore_Id(Long fromStoreId, Long toStoreId);
}
