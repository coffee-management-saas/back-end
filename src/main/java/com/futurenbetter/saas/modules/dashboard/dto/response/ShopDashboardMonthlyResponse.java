package com.futurenbetter.saas.modules.dashboard.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@Data
@Builder
public class ShopDashboardMonthlyResponse {
    private Long id;
    private Month reportMonth;
    private Long totalRevenue;
    private Integer totalOrders;
    private Double usingPromotionRate;
    private List<TopDailyProductResponse> topProducts;
}
