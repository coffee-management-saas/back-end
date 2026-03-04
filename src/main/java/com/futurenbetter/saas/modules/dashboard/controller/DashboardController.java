package com.futurenbetter.saas.modules.dashboard.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.modules.dashboard.dto.filter.MonthlyProductSoldFilter;
import com.futurenbetter.saas.modules.dashboard.dto.response.ShopDashboardResponse;
import com.futurenbetter.saas.modules.dashboard.dto.response.MonthlyProductSoldResponse;
import com.futurenbetter.saas.modules.dashboard.service.inter.MonthlyProductSoldService;
import com.futurenbetter.saas.modules.dashboard.service.inter.ShopDashboardService;
import com.futurenbetter.saas.modules.dashboard.service.inter.SystemDashboardService;
import com.futurenbetter.saas.modules.dashboard.task.DashboardTask;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    //private final ShopDashboardService shopDashboardService;
    private final SystemDashboardService systemDashboardService;
    private final MonthlyProductSoldService monthlyProductSoldService;
    private final ShopDashboardService shopDashboardService;
    private final DashboardTask dashboardTask;

    @GetMapping("/shop/overview/dashboard")
    @PreAuthorize("hasAuthority('dashboard:shop-daily')")
    public ApiResponse<List<ShopDashboardResponse>> getDailyDashboard(@RequestParam int year) {
        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy dữ liệu Shop Dashboard theo ngày thành công",
                shopDashboardService.getAll(year),
                null
        );
    }

    @GetMapping("/shop/overview/trigger")
    @PreAuthorize("hasAuthority('dashboard:shop-daily')")
    public ApiResponse<Void> trigger() {
        dashboardTask.updateShopDashboardDaily();
        return ApiResponse.success(
                HttpStatus.OK,
                "Trigger thành công",
                null,
                null
        );
    }

    @GetMapping("/shop/overview/top-products")
    @PreAuthorize("hasAuthority('dashboard:shop')")
    public ApiResponse<List<MonthlyProductSoldResponse>> getShopOverview(MonthlyProductSoldFilter filter) {
        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy dữ liệu Shop Dashboard thành công",
                monthlyProductSoldService.getAll(filter),
                null
        );
    }

//    @GetMapping("/system/overview")
//    @PreAuthorize("hasAuthority('dashboard:system')")
//    public ApiResponse<SystemDashboardResponse> getSystemOverview(DashboardFilter filter) {
//        return ApiResponse.success(
//                HttpStatus.OK,
//                "Lấy dữ liệu System Dashboard thành công",
//                null,
//                null
//        );
//    }
}
