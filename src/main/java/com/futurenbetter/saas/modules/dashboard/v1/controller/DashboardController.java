package com.futurenbetter.saas.modules.dashboard.v1.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.modules.dashboard.v1.dto.filter.MonthlyProductSoldFilter;
import com.futurenbetter.saas.modules.dashboard.v1.dto.response.ShopDashboardResponse;
import com.futurenbetter.saas.modules.dashboard.v1.dto.response.MonthlyProductSoldResponse;
import com.futurenbetter.saas.modules.dashboard.v1.dto.response.SystemDashboardResponse;
import com.futurenbetter.saas.modules.dashboard.v1.service.inter.MonthlyProductSoldService;
import com.futurenbetter.saas.modules.dashboard.v1.service.inter.ShopDashboardService;
import com.futurenbetter.saas.modules.dashboard.v1.service.inter.SystemDashboardService;
import com.futurenbetter.saas.modules.dashboard.v1.task.DashboardTask;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    //private final ShopDashboardService shopDashboardService;
    private final SystemDashboardService systemDashboardService;
    private final MonthlyProductSoldService monthlyProductSoldService;
    private final ShopDashboardService shopDashboardService;
    private final DashboardTask dashboardTask;

    @GetMapping("/shop/overview/dashboard")
    //@PreAuthorize("hasAuthority('dashboard:shop-daily')")
    public ApiResponse<List<ShopDashboardResponse>> getShopDailyDashboard(@RequestParam int year) {
        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy dữ liệu Shop Dashboard theo ngày thành công",
                shopDashboardService.getAll(year),
                null
        );
    }

    @GetMapping("/shop/overview/trigger")
    //@PreAuthorize("hasAuthority('dashboard:shop-daily')")
    public ApiResponse<Void> triggerShop() {
        dashboardTask.updateShopDashboardDaily();
        return ApiResponse.success(
                HttpStatus.OK,
                "Trigger shop thành công",
                null,
                null
        );
    }

    @GetMapping("/shop/overview/top-products")
    //@PreAuthorize("hasAuthority('dashboard:shop')")
    public ApiResponse<List<MonthlyProductSoldResponse>> getShopOverview(MonthlyProductSoldFilter filter) {
        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy dữ liệu Shop Dashboard thành công",
                monthlyProductSoldService.getAll(filter),
                null
        );
    }


    @GetMapping("/system/overview/dashboard")
    //@PreAuthorize("hasAuthority('dashboard:system')")
    public ApiResponse<List<SystemDashboardResponse>> getSystemDailyDashboard(@RequestParam int year) {
        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy dữ liệu System Dashboard theo ngày thành công",
                systemDashboardService.getAll(year),
                null
        );
    }

    @GetMapping("/system/overview/trigger")
    //@PreAuthorize("hasAuthority('dashboard:system')")
    public ApiResponse<Void> triggerSystem() {
        dashboardTask.updateSystemDashboardDaily();
        return ApiResponse.success(
                HttpStatus.OK,
                "Trigger system thành công",
                null,
                null
        );
    }
}
