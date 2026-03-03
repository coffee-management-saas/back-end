package com.futurenbetter.saas.modules.dashboard.task;

import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.auth.service.CustomerService;
import com.futurenbetter.saas.modules.dashboard.entity.Dashboard;
import com.futurenbetter.saas.modules.dashboard.repository.DashboardRepository;
import com.futurenbetter.saas.modules.order.enums.OrderStatus;
import com.futurenbetter.saas.modules.order.enums.OrderType;
import com.futurenbetter.saas.modules.order.repository.OrderRepository;
import com.futurenbetter.saas.modules.order.service.OrderService;
import com.futurenbetter.saas.modules.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
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


    @Scheduled(cron = "0 5 0 * * ?")
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

            Long totalRevenue = orderRepository.calculateTotalRevenueByShop(shop.getId(),OrderStatus.PAID , start, end);
            System.out.println(totalRevenue);
            Integer totalOrders = orderRepository.countOrdersByShop(shop.getId(), start, end);
            Integer totalProducts = productRepository.countByShopId(shop.getId());
            Integer newCustomers = customerService.countNewCustomers(shop.getId(), startOfMonth, endOfMonth);
            Integer returningCustomers = customerService.countReturningCustomers(shop.getId(), startOfMonth, endOfMonth);
            Integer totalOfflineOrders = orderRepository.countAllByOrderStatusAndOrderTypeAndShopIdAndCreatedAtBetween(OrderStatus.PAID, OrderType.OFFLINE, shop.getId(), start, end);
            Integer totalOnlineOrders = orderRepository.countAllByOrderStatusAndOrderTypeAndShopIdAndCreatedAtBetween(OrderStatus.PAID, OrderType.ONLINE, shop.getId(), start, end);

            if(totalRevenue == null) {
                totalRevenue = 0l;
            }

            if(currentDashboard == null) { // create

                Dashboard newDashboard = Dashboard.builder()
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

                dashboardRepository.save(newDashboard);
            } else { // update

                currentDashboard.setTotalRevenue(((currentDashboard.getTotalRevenue() != null) ? currentDashboard.getTotalRevenue() : 0) + totalRevenue);
                currentDashboard.setTotalOrders(((currentDashboard.getTotalOrders() != null) ? currentDashboard.getTotalOrders() : 0) + totalOrders);
                currentDashboard.setTotalProduct(totalProducts);
                currentDashboard.setNewCustomers(newCustomers);
                currentDashboard.setReturningCustomers(returningCustomers);
                currentDashboard.setTotalOfflineOrders(((currentDashboard.getTotalOfflineOrders() != null) ? currentDashboard.getTotalOfflineOrders() : 0) + totalOfflineOrders);
                currentDashboard.setTotalOnlineOrders(((currentDashboard.getTotalOnlineOrders() != null) ? currentDashboard.getTotalOnlineOrders() : 0) + totalOnlineOrders);

                dashboardRepository.save(currentDashboard);
            }
            System.out.println(shop.getId() + " | Total Revenue: " + totalRevenue + " | Total Orders: " + totalOrders + " | Total Products: " + totalProducts + " | New Customers: " + newCustomers + " | Returning Customers: " + returningCustomers + " | Total Offline Orders: " + totalOfflineOrders + " | Total Online Orders: " + totalOnlineOrders);
        });
    }
}
