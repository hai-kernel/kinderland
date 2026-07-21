package kinderland.order.controller;

import kinderland.order.model.entity.OrderStatus;
import kinderland.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal API — product-service gọi qua Feign để kiểm tra khách đã mua SKU (ràng buộc Review).
 * KHÔNG route qua Gateway. "Đã mua" = có đơn chứa SKU ở trạng thái PAID/SHIPPING/COMPLETED.
 */
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class OrderInternalController {

    private static final List<OrderStatus> PURCHASED_STATUSES =
            List.of(OrderStatus.PAID, OrderStatus.SHIPPING, OrderStatus.DELIVERED, OrderStatus.COMPLETED);

    private final OrderItemRepository orderItemRepository;

    @GetMapping("/purchased")
    public boolean hasPurchased(@RequestParam String email, @RequestParam Long skuId) {
        return orderItemRepository.hasPurchasedSku(email, skuId, PURCHASED_STATUSES);
    }
}
