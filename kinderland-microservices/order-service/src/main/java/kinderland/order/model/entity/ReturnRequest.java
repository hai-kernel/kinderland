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

/**
 * Yêu cầu trả hàng/hoàn tiền. Keyed by customerEmail/processedByEmail (String) thay FK Account.
 * OrderItem cùng service nên vẫn giữ @ManyToOne. Ảnh minh hoạ lưu ngay tại order-service
 * (order-service không có Image/S3 như product) qua @ElementCollection.
 */
@Entity
@Table(name = "return_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnId;

    @Column(name = "return_code", unique = true)
    private String returnCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItem orderItem;

    /** Khách hàng yêu cầu (email = subject JWT). */
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    /** Nhân viên/quản lý xử lý (email). */
    @Column(name = "processed_by")
    private String processedByEmail;

    @Column(name = "return_reason", nullable = false)
    private String returnReason;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_status", nullable = false)
    private ReturnStatus returnStatus;

    @Column(name = "description")
    private String description;

    /** URL ảnh minh hoạ (FE upload trước qua imageApi rồi gửi kèm). */
    @ElementCollection
    @CollectionTable(name = "return_request_photos", joinColumns = @JoinColumn(name = "return_id"))
    @Column(name = "photo_url")
    @Builder.Default
    private List<String> photoUrls = new ArrayList<>();

    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_type")
    private String refundType;

    // Thông tin ngân hàng khách cung cấp để hoàn tiền
    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_name")
    private String bankAccountName;

    /** Mã giao dịch chuyển khoản của quản lý (bằng chứng đã hoàn tiền). */
    @Column(name = "refund_transaction_code")
    private String refundTransactionCode;

    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private LocalDateTime refundedAt;
}
