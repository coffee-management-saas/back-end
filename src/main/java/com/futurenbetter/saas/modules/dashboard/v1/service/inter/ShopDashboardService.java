package com.futurenbetter.saas.modules.dashboard.v1.service.inter;

import com.futurenbetter.saas.modules.dashboard.v1.dto.projection.BestSellerProjection;
import com.futurenbetter.saas.modules.dashboard.v1.dto.response.ShopDashboardResponse;

import java.util.List;

public interface ShopDashboardService {
    List<BestSellerProjection> getBestSeller(Long shopId, int limit);
    List<ShopDashboardResponse> getAll(int year);
}