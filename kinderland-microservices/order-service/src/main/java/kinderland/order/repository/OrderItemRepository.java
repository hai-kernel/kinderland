package kinderland.order.repository;

import kinderland.order.model.entity.OrderItem;
import kinderland.order.model.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /** Khách (email) đã mua SKU này trong đơn có trạng thái thuộc statuses chưa (cho Review đã-mua-check). */
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi " +
            "WHERE oi.order.accountEmail = :email AND oi.skuId = :skuId AND oi.order.status IN :statuses")
    boolean hasPurchasedSku(@Param("email") String email,
                            @Param("skuId") Long skuId,
                            @Param("statuses") Collection<OrderStatus> statuses);
}
