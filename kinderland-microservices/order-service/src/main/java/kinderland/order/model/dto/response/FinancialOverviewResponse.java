package kinderland.order.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** Khớp FE financialApi.FinancialOverviewData. */
@Data
@Builder
public class FinancialOverviewResponse {
    private BigDecimal totalRevenue;         // doanh thu gộp (đơn COMPLETED)
    private BigDecimal todayRevenue;         // doanh thu gộp hôm nay
    private BigDecimal thisMonthRevenue;     // doanh thu gộp tháng này
    private BigDecimal totalRefunds;         // tổng tiền đã hoàn
    private BigDecimal todayRefunds;         // tiền hoàn hôm nay
    private BigDecimal thisMonthRefunds;     // tiền hoàn tháng này
    private BigDecimal netRevenue;           // totalRevenue - totalRefunds
    private BigDecimal todayNetRevenue;      // todayRevenue - todayRefunds
    private BigDecimal thisMonthNetRevenue;  // thisMonthRevenue - thisMonthRefunds
}
