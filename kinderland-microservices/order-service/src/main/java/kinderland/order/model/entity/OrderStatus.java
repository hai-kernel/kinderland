package kinderland.order.model.entity;

public enum OrderStatus {
    PENDING,
    PAID,
    SHIPPING,
    DELIVERED,   // đã giao tới khách — FE cho phép yêu cầu trả hàng ở trạng thái này
    COMPLETED,
    CANCELLED
}
