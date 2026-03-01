package com.futurenbetter.saas.modules.dashboard.service.inter;

import com.futurenbetter.saas.modules.dashboard.dto.filter.DashboardFilter;
import com.futurenbetter.saas.modules.dashboard.dto.projection.BestSellerProjection;
import com.futurenbetter.saas.modules.dashboard.dto.response.ShopDashboardDailyResponse;
import com.futurenbetter.saas.modules.dashboard.dto.response.ShopDashboardResponse;

import java.util.List;

public interface ShopDashboardService {
    List<ShopDashboardDailyResponse> getDaily(DashboardFilter filter);
    ShopDashboardResponse getShopDashboard(DashboardFilter filter);
    List<BestSellerProjection> getBestSeller(Long shopId, int limit);
}