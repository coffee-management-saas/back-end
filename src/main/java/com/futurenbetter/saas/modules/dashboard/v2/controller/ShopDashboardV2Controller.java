package com.futurenbetter.saas.modules.dashboard.v2.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.dashboard.v2.dto.response.ShopDashboardResponseV2;
import com.futurenbetter.saas.modules.dashboard.v2.enums.ReportType;
import com.futurenbetter.saas.modules.dashboard.v2.service.ShopDashboardV2Service;
import com.futurenbetter.saas.modules.dashboard.v2.task.DashboardV2AggregationTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v2/dashboard/shop")
@RequiredArgsConstructor
public class ShopDashboardV2Controller {

    private final ShopDashboardV2Service dashboardService;
    private final DashboardV2AggregationTask dashboardV2AggregationTask;

    @GetMapping("/stats")
    public ApiResponse<ShopDashboardResponseV2> getStats
            (
                    @RequestParam ReportType type,
                    @Parameter(description = "Định dạng YYYY-MM-DD")
                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate
            ) {

        if (targetDate == null) {
            targetDate = LocalDate.now();
        }

        Long shopId = TenantContext.getCurrentShopId();

        ShopDashboardResponseV2 data = dashboardService.getDashboardData(shopId, type, targetDate);

        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy dữ liệu Shop Dashboard theo ngày thành công",
                data,
                null
        );
    }

    @PostMapping("/trigger")
    public ApiResponse<String> triggerSync(
            @Parameter(description = "Định dạng YYYY-MM-DD")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {

        Long shopId = TenantContext.getCurrentShopId();
        dashboardService.aggregateShopData(shopId, targetDate);

        return ApiResponse.success(
                HttpStatus.OK,
                "Trigger Shop Dashboard theo ngày thành công",
                null,
                null);
    }
}
