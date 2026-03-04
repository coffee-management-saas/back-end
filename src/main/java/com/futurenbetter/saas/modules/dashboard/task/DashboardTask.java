package com.futurenbetter.saas.modules.dashboard.task;

import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.auth.service.CustomerService;
import com.futurenbetter.saas.modules.dashboard.entity.ShopDashboard;
import com.futurenbetter.saas.modules.dashboard.repository.ShopDashboardRepository;
import com.futurenbetter.saas.modules.order.enums.OrderStatus;
import com.futurenbetter.saas.modules.order.enums.OrderType;
import com.futurenbetter.saas.modules.order.repository.OrderRepository;
import com.futurenbetter.saas.modules.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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

    private final ShopDashboardRepository shopDashboardRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerService customerService;


    @Scheduled(cron = "0 5 0 * * ?")
    @Transactional
    public void updateShopDashboardDaily() {

        log.info("[DashboardTask] Bắt đầu tổng hợp dữ liệu Dashboard Daily...");

        Integer year = YearMonth.now().getYear();
        Month month = YearMonth.now().getMonth();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(23, 59, 59, 999999999);
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59, 999999999);

        shopRepository.findAll().forEach(shop -> {
            ShopDashboard currentShopDashboard = shopDashboardRepository.findByShopIdAndYearAndMonth(shop.getId(), year, month).orElse(null);
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

            if(currentShopDashboard == null) { // create

                currentShopDashboard = ShopDashboard.builder()
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

                shopDashboardRepository.save(currentShopDashboard);
            } else { // update

                currentShopDashboard.setTotalRevenue((totalRevenue != null) ? totalRevenue : 0);
                currentShopDashboard.setTotalOrders((totalOrders != null) ? totalOrders : 0);
                currentShopDashboard.setTotalProduct(totalProducts);
                currentShopDashboard.setNewCustomers(newCustomers);
                currentShopDashboard.setReturningCustomers(returningCustomers);
                currentShopDashboard.setTotalOfflineOrders((totalOfflineOrders != null) ? totalOfflineOrders : 0);
                currentShopDashboard.setTotalOnlineOrders((totalOnlineOrders != null) ? totalOnlineOrders : 0);

                shopDashboardRepository.save(currentShopDashboard);
            }
        });
    }
}
