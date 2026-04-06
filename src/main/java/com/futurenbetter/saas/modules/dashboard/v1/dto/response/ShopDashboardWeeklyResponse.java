package com.futurenbetter.saas.modules.dashboard.v1.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ShopDashboardWeeklyResponse {
    private Long id;
    private LocalDate startOfWeek;
    private Long totalRevenue;
    private Integer totalOrders;
    private Double usingPromotionRate;
    private List<TopDailyProductResponse> topProducts;
}
