package com.futurenbetter.saas.modules.dashboard.service.impl;

import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.dashboard.dto.filter.DashboardFilter;
import com.futurenbetter.saas.modules.dashboard.dto.response.ShopDashboardResponse;
import com.futurenbetter.saas.modules.dashboard.repository.ShopDailyReportRepository;
import com.futurenbetter.saas.modules.dashboard.service.inter.ShopDashboardService;
import com.futurenbetter.saas.modules.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShopDashboardServiceImpl implements ShopDashboardService {

    private final OrderRepository orderRepository;
    private final ShopDailyReportRepository shopDailyReportRepository;

    @Override
    public ShopDashboardResponse getOverview(DashboardFilter filter) {
        Long shopId = SecurityUtils.getCurrentShopId();

        LocalDate fromDate = filter.getFromDate() != null ? filter.getFromDate() : LocalDate.now().minusDays(30);
        LocalDate toDate = filter.getToDate() != null ? filter.getToDate() : LocalDate.now();
        LocalDate today = LocalDate.now();

        long totalRevenue = 0L;
        int totalOrders = 0;

        // 1. DATA QUÁ KHỨ (Lấy từ bảng Daily Report)
        LocalDate endOfPastDate = toDate.isBefore(today) ? toDate : today.minusDays(1);

        if (!fromDate.isAfter(endOfPastDate)) {
            Long pastRev = shopDailyReportRepository.sumRevenue(shopId, fromDate, endOfPastDate);
            Integer pastOrd = shopDailyReportRepository.sumOrders(shopId, fromDate, endOfPastDate);

            totalRevenue += (pastRev != null ? pastRev : 0L);
            totalOrders += (pastOrd != null ? pastOrd : 0);
        }

        // 2. DATA HÔM NAY (Lấy Realtime từ bảng Order)
        if (!toDate.isBefore(today) && !fromDate.isAfter(today)) {
            LocalDateTime startOfToday = today.atStartOfDay();
            LocalDateTime endOfToday = today.atTime(23, 59, 59, 999999999);

            Long liveRev = orderRepository.calculateTotalRevenueByShop(shopId, startOfToday, endOfToday);
            Integer liveOrd = orderRepository.countOrdersByShop(shopId, startOfToday, endOfToday);

            totalRevenue += (liveRev != null ? liveRev : 0L);
            totalOrders += (liveOrd != null ? liveOrd : 0);
        }

        return ShopDashboardResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .build();
    }
}
