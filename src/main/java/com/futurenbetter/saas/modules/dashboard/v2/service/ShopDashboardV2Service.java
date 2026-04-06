package com.futurenbetter.saas.modules.dashboard.v2.service;

import com.futurenbetter.saas.modules.dashboard.v2.dto.response.ShopDashboardResponseV2;
import com.futurenbetter.saas.modules.dashboard.v2.enums.ReportType;

import java.time.LocalDate;

public interface ShopDashboardV2Service {
    ShopDashboardResponseV2 getDashboardData(Long shopId, ReportType type, LocalDate targetDate);
    ShopDashboardResponseV2 getHistoricalReport(Long shopId, ReportType type, LocalDate targetDate);
    ShopDashboardResponseV2 getWeeklyChartData(Long shopId, LocalDate targetDate);
    ShopDashboardResponseV2 getMonthlyChartData(Long shopId, LocalDate targetDate);
    ShopDashboardResponseV2 getYearlyChartData(Long shopId, LocalDate targetDate);
    void aggregateShopData(Long shopId, LocalDate targetDate);
    void aggregateAllShops(LocalDate targetDate);
}
