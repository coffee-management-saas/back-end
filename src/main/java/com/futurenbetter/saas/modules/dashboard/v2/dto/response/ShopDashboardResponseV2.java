package com.futurenbetter.saas.modules.dashboard.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopDashboardResponseV2 {
    private String period;          // "Hôm nay", "Tuần 15", "Tháng 04/2024"
    private Double summaryRevenue;  // Tổng doanh thu kỳ này
    private Long summaryOrders;     // Tổng đơn hàng kỳ này

    private List<ChartDataPoint> chartData;

    @Data
    @AllArgsConstructor
    public static class ChartDataPoint {
        private String label;  // "08:00", "Thứ Hai", "Ngày 15", "Tháng 05"
        private Double revenue;
        private Long orderCount;
    }
}
