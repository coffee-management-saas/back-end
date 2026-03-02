package com.futurenbetter.saas.modules.dashboard.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.modules.dashboard.dto.filter.DashboardFilter;
import com.futurenbetter.saas.modules.dashboard.dto.projection.BestSellerProjection;
import com.futurenbetter.saas.modules.dashboard.dto.response.ShopDashboardDailyResponse;
import com.futurenbetter.saas.modules.dashboard.dto.response.ShopDashboardResponse;
import com.futurenbetter.saas.modules.dashboard.dto.response.SystemDashboardResponse;
import com.futurenbetter.saas.modules.dashboard.service.inter.ShopDashboardService;
import com.futurenbetter.saas.modules.dashboard.service.inter.SystemDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ShopDashboardService shopDashboardService;
    private final SystemDashboardService systemDashboardService;

    @GetMapping("/shop/overview/daily")
    @PreAuthorize("hasAuthority('dashboard:shop-daily')")
    public ApiResponse<List<ShopDashboardDailyResponse>> getDailyDashboard(DashboardFilter filter) {
        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy dữ liệu Shop Dashboard theo ngày thành công",
                shopDashboardService.getDaily(filter),
                null
        );
    }

    @GetMapping("/shop/overview")
    @PreAuthorize("hasAuthority('dashboard:shop')")
    public ApiResponse<ShopDashboardResponse> getShopOverview(DashboardFilter filter) {
        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy dữ liệu Shop Dashboard thành công",
                shopDashboardService.getShopDashboard(filter),
                null
        );
    }

    @GetMapping("/system/overview")
    @PreAuthorize("hasAuthority('dashboard:system')")
    public ApiResponse<SystemDashboardResponse> getSystemOverview(DashboardFilter filter) {
        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy dữ liệu System Dashboard thành công",
                systemDashboardService.getOverview(filter),
                null
        );
    }
}
