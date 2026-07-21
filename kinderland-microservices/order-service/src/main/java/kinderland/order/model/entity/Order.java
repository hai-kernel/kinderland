package kinderland.order.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Định danh khách hàng = email (subject JWT, do Gateway truyền qua header). KHÔNG còn FK Account. */
    @Column(nullable = false)
    private String accountEmail;

    /** Cửa hàng nhận đơn + địa chỉ giao (tham chiếu lỏng; addressId thuộc auth-service). */
    private Long storeId;
    private Long addressId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    /** Điểm loyalty đã dùng khi checkout + số VND được giảm tương ứng (0 nếu không dùng). */
    @Builder.Default
    private Integer pointsUsed = 0;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal pointsDiscount = BigDecimal.ZERO;

    /** Đánh dấu đã tích điểm cho đơn này (tránh tích 2 lần khi cập nhật trạng thái).
     *  columnDefinition có DEFAULT để ddl-auto=update thêm được cột NOT NULL vào bảng orders đã có dữ liệu. */
    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean pointsAwarded = false;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;
}
