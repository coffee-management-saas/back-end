package com.futurenbetter.saas.modules.dashboard.service.impl;

import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.dashboard.dto.filter.DashboardFilter;
import com.futurenbetter.saas.modules.dashboard.dto.response.SystemDashboardResponse;
import com.futurenbetter.saas.modules.dashboard.repository.SystemMonthlyReportRepository;
import com.futurenbetter.saas.modules.dashboard.service.inter.SystemDashboardService;
import com.futurenbetter.saas.modules.subscription.repository.SubscriptionTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SystemDashboardServiceImpl implements SystemDashboardService {

    private final SubscriptionTransactionRepository transactionRepository;
    private final ShopRepository shopRepository;
    private final SystemMonthlyReportRepository monthlyReportRepository;

    @Override
    public SystemDashboardResponse getOverview(DashboardFilter filter) {
        LocalDate fromDate = filter.getFromDate() != null ? filter.getFromDate() : LocalDate.now().minusMonths(6).withDayOfMonth(1);
        LocalDate toDate = filter.getToDate() != null ? filter.getToDate() : LocalDate.now();
        LocalDate thisMonthStart = LocalDate.now().withDayOfMonth(1);

        long totalRev = 0L;
        int totalShops = 0;

        // 1. Dữ liệu các tháng trước
        if (fromDate.isBefore(thisMonthStart)) {
            LocalDate endOfPast = toDate.isBefore(thisMonthStart) ? toDate : thisMonthStart.minusDays(1);
            Long pastRev = monthlyReportRepository.sumRevenue(
                    fromDate.getMonthValue(), fromDate.getYear(), endOfPast.getMonthValue(), endOfPast.getYear());
            Integer pastShops = monthlyReportRepository.sumNewShops(
                    fromDate.getMonthValue(), fromDate.getYear(), endOfPast.getMonthValue(), endOfPast.getYear());

            totalRev += (pastRev != null ? pastRev : 0L);
            totalShops += (pastShops != null ? pastShops : 0);
        }

        // 2. Dữ liệu tháng hiện tại (Realtime)
        if (!toDate.isBefore(thisMonthStart)) {
            LocalDateTime startOfThisMonth = thisMonthStart.atStartOfDay();
            LocalDateTime endOfPeriod = toDate.atTime(23, 59, 59, 999999999);

            Long liveRev = transactionRepository.calculateTotalSystemRevenue(startOfThisMonth, endOfPeriod);
            Integer liveShops = shopRepository.countNewShops(startOfThisMonth, endOfPeriod);

            totalRev += (liveRev != null ? liveRev : 0L);
            totalShops += (liveShops != null ? liveShops : 0);
        }

        return SystemDashboardResponse.builder()
                .totalSubscriptionRevenue(totalRev)
                .totalNewShops(totalShops)
                .build();
    }
}
