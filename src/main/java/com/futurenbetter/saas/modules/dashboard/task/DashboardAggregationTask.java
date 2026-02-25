package com.futurenbetter.saas.modules.dashboard.task;

import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.dashboard.entity.ShopDailyReport;
import com.futurenbetter.saas.modules.dashboard.entity.SystemMonthlyReport;
import com.futurenbetter.saas.modules.dashboard.repository.ShopDailyReportRepository;
import com.futurenbetter.saas.modules.dashboard.repository.SystemMonthlyReportRepository;
import com.futurenbetter.saas.modules.order.repository.OrderRepository;
import com.futurenbetter.saas.modules.subscription.repository.SubscriptionTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardAggregationTask {

    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final ShopDailyReportRepository shopDailyReportRepository;
    private final SystemMonthlyReportRepository systemMonthlyReportRepository;
    private final SubscriptionTransactionRepository transactionRepository;

    /**
     * Chạy lúc 00:05 mỗi ngày. Chốt số của "Hôm qua".
     */
    @Scheduled(cron = "0 5 0 * * ?")
    @Transactional
    public void aggregateShopDailyData() {
        log.info("[CronJob] Bắt đầu tổng hợp dữ liệu Shop Daily...");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(23, 59, 59, 999999999);

        shopRepository.findAll().forEach(shop -> {
            Long totalRev = orderRepository.calculateTotalRevenueByShop(shop.getId(), start, end);
            Integer totalOrd = orderRepository.countOrdersByShop(shop.getId(), start, end);
            Integer totalOrdWithPromotion = orderRepository.countOdersByShopIdAndHasPromotionIsTrue(shop.getId(), start, end);

            // Bỏ qua nếu ngày hôm đó quán không có doanh thu để tránh rác DB
            if ((totalRev != null && totalRev > 0) || (totalOrd != null && totalOrd > 0)) {
                ShopDailyReport report = ShopDailyReport.builder()
                        .shop(shop)
                        .reportDate(yesterday)
                        .totalRevenue(totalRev != null ? totalRev : 0L)
                        .totalOrders(totalOrd != null ? totalOrd : 0)
                        .usingVouchersPercentage(totalOrd != null && totalOrd > 0 ? (totalOrdWithPromotion != null ? totalOrdWithPromotion : 0) * 100.0 / totalOrd : 0.0)
                        .build();
                shopDailyReportRepository.save(report);
            }
        });
        log.info("[CronJob] Hoàn thành tổng hợp Shop Daily.");
    }

    /**
     * Chạy lúc 00:10 ngày mùng 1 hàng tháng. Chốt số của "Tháng trước".
     */
    @Scheduled(cron = "0 10 0 1 * ?")
    @Transactional
    public void aggregateSystemMonthlyData() {
        log.info("[CronJob] Bắt đầu tổng hợp dữ liệu System Monthly...");
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        LocalDateTime start = lastMonth.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).atTime(23, 59, 59, 999999999);

        Long totalRev = transactionRepository.calculateTotalSystemRevenue(start, end);
        Integer newShops = shopRepository.countNewShops(start, end);

        SystemMonthlyReport report = SystemMonthlyReport.builder()
                .reportMonth(lastMonth.getMonthValue())
                .reportYear(lastMonth.getYear())
                .totalRevenue(totalRev != null ? totalRev : 0L)
                .newShopsCount(newShops != null ? newShops : 0)
                .build();

        systemMonthlyReportRepository.save(report);
        log.info("[CronJob] Hoàn thành tổng hợp System Monthly.");
    }
}
