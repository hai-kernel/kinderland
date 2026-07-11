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

/**
 * Báo cáo tài chính, port từ monolith. Doanh thu gộp = tổng đơn COMPLETED (mono dùng DELIVERED);
 * refund = tổng return REFUNDED; net = gộp - refund.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FinancialService {

    OrderRepository orderRepository;
    ReturnRequestRepository returnRequestRepository;

    public FinancialOverviewResponse getOverview() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).atTime(23, 59, 59);

        // ── Doanh thu gộp (đơn COMPLETED) ──
        BigDecimal totalRevenue = nz(orderRepository.getTotalRevenue(OrderStatus.COMPLETED));
        BigDecimal todayRevenue = nz(orderRepository.getRevenueByDateRange(OrderStatus.COMPLETED, startOfDay, endOfDay));
        BigDecimal thisMonthRevenue = nz(orderRepository.getRevenueByDateRange(OrderStatus.COMPLETED, startOfMonth, endOfMonth));

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
        BigDecimal gross = nz(orderRepository.getRevenueByDateRange(OrderStatus.COMPLETED, startDt, endDt));
        BigDecimal refunds = nz(returnRequestRepository.getRefundAmountByDateRange(ReturnStatus.REFUNDED, startDt, endDt));
        return gross.subtract(refunds);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
