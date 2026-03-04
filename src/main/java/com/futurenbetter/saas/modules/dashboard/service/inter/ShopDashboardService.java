package com.futurenbetter.saas.modules.dashboard.service.inter;

import com.futurenbetter.saas.modules.dashboard.dto.projection.BestSellerProjection;
import com.futurenbetter.saas.modules.dashboard.dto.response.ShopDashboardResponse;

import java.util.List;

public interface ShopDashboardService {
    List<BestSellerProjection> getBestSeller(Long shopId, int limit);
    List<ShopDashboardResponse> getAll(int year);
}