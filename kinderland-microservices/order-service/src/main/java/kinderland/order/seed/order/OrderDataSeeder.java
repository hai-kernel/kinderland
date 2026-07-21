package kinderland.order.seed.order;

import kinderland.common.seed.AbstractDataSeeder;
import kinderland.order.model.entity.OrderItem;
import kinderland.order.model.entity.OrderStatus;
import kinderland.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(10)
@ConditionalOnProperty(name = "app.seed.order.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class OrderDataSeeder extends AbstractDataSeeder {

    private final OrderRepository orderRepository;

    @Override
    public String getName() {
        return "OrderDataSeeder";
    }

    @Override
    @Transactional
    public void seed() {
        logStart();
        
        long currentOrderCount = orderRepository.count();
        if (currentOrderCount >= 60) {
            log.info("Already have {} orders. Skipping order seed.", currentOrderCount);
            logCompleted(0, 60);
            return;
        }

        int targetOrders = 60;
        int created = 0;
        int skipped = 0;

        LocalDateTime now = LocalDateTime.now();

        // PENDING, PAID, SHIPPING, DELIVERED, COMPLETED, CANCELLED
        List<OrderStatus> statuses = new ArrayList<>();
        addStatuses(statuses, OrderStatus.PENDING, 10);
        addStatuses(statuses, OrderStatus.PAID, 8);
        addStatuses(statuses, OrderStatus.SHIPPING, 10);
        addStatuses(statuses, OrderStatus.DELIVERED, 8);
        addStatuses(statuses, OrderStatus.COMPLETED, 16);
        addStatuses(statuses, OrderStatus.CANCELLED, 8);

        for (int i = 1; i <= targetOrders; i++) {
            String email = String.format("customer%02d@kinderland.vn", (i % 30) + 1);
            
            // Avoid creating duplicate orders for the same email at the exact same time
            // So we spread them out over 6 months
            LocalDateTime createdAt = now.minusDays(i * 3L).minusHours(i);

            // Check if this specific email already has an order around this time (avoid duplicating in multiple runs)
            List<kinderland.order.model.entity.Order> existing = orderRepository.findByAccountEmailOrderByCreatedAtDesc(email);
            if (existing.size() >= 2) {
                // If a customer already has >= 2 orders, we assume it's seeded
                // This is a heuristic to prevent infinite seeding without checking count() == 0 globally.
                skipped++;
                continue;
            }

            OrderStatus status = statuses.get((i - 1) % statuses.size());
            
            // Deterministic mock product/sku IDs
            long productId = (i % 50) + 1;
            long skuId = productId;
            String skuCode = String.format("SKU-%03d", skuId);
            BigDecimal unitPrice = new BigDecimal(199000 + (i * 10000));
            int quantity = (i % 3) + 1;
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            kinderland.order.model.entity.Order order = kinderland.order.model.entity.Order.builder()
                    .accountEmail(email)
                    .storeId(1L)
                    .addressId((long) (i % 10) + 1)
                    .totalAmount(lineTotal)
                    .pointsUsed(0)
                    .pointsDiscount(BigDecimal.ZERO)
                    .status(status)
                    .createdAt(createdAt)
                    .build();

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .skuId(skuId)
                    .productId(productId)
                    .skuCode(skuCode)
                    .productName("Sản phẩm mẫu " + productId)
                    .unitPrice(unitPrice)
                    .quantity(quantity)
                    .lineTotal(lineTotal)
                    .build();

            order.getItems().add(item);
            
            orderRepository.save(order);
            created++;
        }

        logCompleted(created, skipped);
    }
    
    private void addStatuses(List<OrderStatus> list, OrderStatus status, int count) {
        for (int i = 0; i < count; i++) {
            list.add(status);
        }
    }
}
