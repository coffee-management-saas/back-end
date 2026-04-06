package com.futurenbetter.saas.modules.dashboard.v2.task;

import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.dashboard.v2.entity.ShopReportV2;
import com.futurenbetter.saas.modules.dashboard.v2.enums.ReportType;
import com.futurenbetter.saas.modules.dashboard.v2.repository.ShopReportV2Repository;
import com.futurenbetter.saas.modules.dashboard.v2.service.ShopDashboardV2Service;
import com.futurenbetter.saas.modules.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DashboardV2AggregationTask {
    private final ShopDashboardV2Service dashboardService;

    @Scheduled(cron = "0 5 0 * * *")
    public void runDaily() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        dashboardService.aggregateAllShops(yesterday);
    }
}
