package com.futurenbetter.saas.modules.dashboard.task;

import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.auth.service.CustomerService;
import com.futurenbetter.saas.modules.dashboard.entity.Dashboard;
import com.futurenbetter.saas.modules.dashboard.repository.DashboardRepository;
import com.futurenbetter.saas.modules.order.enums.OrderStatus;
import com.futurenbetter.saas.modules.order.enums.OrderType;
import com.futurenbetter.saas.modules.order.repository.OrderRepository;
import com.futurenbetter.saas.modules.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardTask {

    private final DashboardRepository dashboardRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerService customerService;


    //@Scheduled(cron = "0 5 0 * * ?")
    @Transactional
    public void updateDashboardDaily() {

        log.info("[DashboardTask] Bắt đầu tổng hợp dữ liệu Dashboard Daily...");

        Integer year = YearMonth.now().getYear();
        Month month = YearMonth.now().getMonth();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(23, 59, 59, 999999999);
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59, 999999999);

        shopRepository.findAll().forEach(shop -> {
            Dashboard currentDashboard = dashboardRepository.findByShopIdAndYearAndMonth(shop.getId(), year, month).orElse(null);
            Long totalRevenue = orderRepository.calculateTotalRevenueByShop(shop.getId(),OrderStatus.PAID , startOfMonth, endOfMonth);
            Integer totalOrders = orderRepository.countOrdersByShop(shop.getId(), startOfMonth, endOfMonth);
            Integer totalProducts = productRepository.countByShopId(shop.getId());
            Integer newCustomers = customerService.countNewCustomers(shop.getId(), startOfMonth, endOfMonth);
            Integer returningCustomers = customerService.countReturningCustomers(shop.getId(), startOfMonth, endOfMonth);
            Integer totalOfflineOrders = orderRepository.countAllByOrderStatusAndOrderTypeAndShopIdAndCreatedAtBetween(OrderStatus.PAID, OrderType.OFFLINE, shop.getId(), startOfMonth, endOfMonth);
            Integer totalOnlineOrders = orderRepository.countAllByOrderStatusAndOrderTypeAndShopIdAndCreatedAtBetween(OrderStatus.PAID, OrderType.ONLINE, shop.getId(), startOfMonth, endOfMonth);

            if(totalRevenue == null) {
                totalRevenue = 0l;
            }

            if(currentDashboard == null) { // create

                currentDashboard = Dashboard.builder()
                        .shop(shop)
                        .year(year)
                        .month(month)
                        .totalRevenue(totalRevenue)
                        .totalOrders(totalOrders)
                        .totalProduct(totalProducts)
                        .newCustomers(newCustomers)
                        .returningCustomers(returningCustomers)
                        .totalOfflineOrders(totalOfflineOrders)
                        .totalOnlineOrders(totalOnlineOrders)
                        .build();

                dashboardRepository.save(currentDashboard);
            } else { // update

                currentDashboard.setTotalRevenue((totalRevenue != null) ? totalRevenue : 0);
                currentDashboard.setTotalOrders((totalOrders != null) ? totalOrders : 0);
                currentDashboard.setTotalProduct(totalProducts);
                currentDashboard.setNewCustomers(newCustomers);
                currentDashboard.setReturningCustomers(returningCustomers);
                currentDashboard.setTotalOfflineOrders((totalOfflineOrders != null) ? totalOfflineOrders : 0);
                currentDashboard.setTotalOnlineOrders((totalOnlineOrders != null) ? totalOnlineOrders : 0);

                dashboardRepository.save(currentDashboard);
            }
        });
    }
}
