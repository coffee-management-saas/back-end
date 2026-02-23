package com.futurenbetter.saas.modules.dashboard.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopDashboardResponse {
    private Long totalRevenue;
    private Integer totalOrders;
}
