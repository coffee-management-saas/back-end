package com.futurenbetter.saas.modules.dashboard.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ShopDashboardResponse {
    private List<ShopDashboardWeeklyResponse> weeklyReports;
    private List<ShopDashboardMonthlyResponse> monthlyReports;
}
