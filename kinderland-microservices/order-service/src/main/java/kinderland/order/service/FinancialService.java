package kinderland.order.service;

import kinderland.order.model.dto.response.FinancialOverviewResponse;
import kinderland.order.model.entity.OrderStatus;
import kinderland.order.model.entity.ReturnStatus;
import kinderland.order.repository.OrderRepository;
import kinderland.order.repository.ReturnRequestRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Báo cáo tài chính, port từ monolith. Doanh thu gộp = tổng đơn COMPLETED (mono dùng DELIVERED);
 * refund = tổng return REFUNDED; net = gộp - refund.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FinancialService {

    /**
     * Các trạng thái được TÍNH LÀ DOANH THU (tiền đã thu).
     *
     * Vòng đời: PENDING -> PAID -> SHIPPING -> DELIVERED -> COMPLETED (hoặc CANCELLED).
     * Trước đây chỉ tính COMPLETED, nên đơn đã thanh toán nhưng chưa giao xong không được
     * tính -> dashboard hiện 0đ dù tiền đã về.
     *
     * LOẠI TRỪ:
     *  - PENDING   : chưa thanh toán
     *  - CANCELLED : đã huỷ, không có tiền
     * Refund được trừ riêng ở netRevenue.
     */
    static final List<OrderStatus> REVENUE_STATUSES = List.of(
            OrderStatus.PAID,
            OrderStatus.SHIPPING,
            OrderStatus.DELIVERED,
            OrderStatus.COMPLETED);

    OrderRepository orderRepository;
    ReturnRequestRepository returnRequestRepository;

    public FinancialOverviewResponse getOverview() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).atTime(23, 59, 59);

        // ── Doanh thu gộp (đơn COMPLETED) ──
        BigDecimal totalRevenue = nz(orderRepository.getTotalRevenue(REVENUE_STATUSES));
        BigDecimal todayRevenue = nz(orderRepository.getRevenueByDateRange(REVENUE_STATUSES, startOfDay, endOfDay));
        BigDecimal thisMonthRevenue = nz(orderRepository.getRevenueByDateRange(REVENUE_STATUSES, startOfMonth, endOfMonth));

        // ── Tiền hoàn (return REFUNDED) ──
        BigDecimal totalRefunds = nz(returnRequestRepository.getTotalRefundAmount(ReturnStatus.REFUNDED));
        BigDecimal todayRefunds = nz(returnRequestRepository.getRefundAmountByDateRange(ReturnStatus.REFUNDED, startOfDay, endOfDay));
        BigDecimal thisMonthRefunds = nz(returnRequestRepository.getRefundAmountByDateRange(ReturnStatus.REFUNDED, startOfMonth, endOfMonth));

        return FinancialOverviewResponse.builder()
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .thisMonthRevenue(thisMonthRevenue)
                .totalRefunds(totalRefunds)
                .todayRefunds(todayRefunds)
                .thisMonthRefunds(thisMonthRefunds)
                .netRevenue(totalRevenue.subtract(totalRefunds))
                .todayNetRevenue(todayRevenue.subtract(todayRefunds))
                .thisMonthNetRevenue(thisMonthRevenue.subtract(thisMonthRefunds))
                .build();
    }

    /** Doanh thu ròng (gộp - hoàn) trong khoảng ngày. */
    public BigDecimal getRevenueByDateRange(LocalDate start, LocalDate end) {
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(23, 59, 59);
        BigDecimal gross = nz(orderRepository.getRevenueByDateRange(REVENUE_STATUSES, startDt, endDt));
        BigDecimal refunds = nz(returnRequestRepository.getRefundAmountByDateRange(ReturnStatus.REFUNDED, startDt, endDt));
        return gross.subtract(refunds);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
