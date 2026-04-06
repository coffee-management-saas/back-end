package com.futurenbetter.saas.modules.dashboard.v1.task;

import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.auth.service.CustomerService;
import com.futurenbetter.saas.modules.dashboard.v1.entity.ShopDashboard;
import com.futurenbetter.saas.modules.dashboard.v1.entity.SystemDashboard;
import com.futurenbetter.saas.modules.dashboard.v1.repository.ShopDashboardRepository;
import com.futurenbetter.saas.modules.dashboard.v1.repository.SystemDashboardRepository;
import com.futurenbetter.saas.modules.order.enums.OrderStatus;
import com.futurenbetter.saas.modules.order.enums.OrderType;
import com.futurenbetter.saas.modules.order.repository.OrderRepository;
import com.futurenbetter.saas.modules.product.repository.ProductRepository;
import com.futurenbetter.saas.modules.subscription.enums.InvoiceEnum;
import com.futurenbetter.saas.modules.subscription.enums.SubscriptionPlanEnum;
import com.futurenbetter.saas.modules.subscription.repository.BillingInvoiceRepository;
import com.futurenbetter.saas.modules.subscription.repository.ShopSubscriptionRepository;
import com.futurenbetter.saas.modules.system.repository.SystemTransactionRepository;
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

    private final CustomerService customerService;
    private final ShopDashboardRepository shopDashboardRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final BillingInvoiceRepository billingInvoiceRepository;
    private final ShopSubscriptionRepository shopSubscriptionRepository;
    private final SystemTransactionRepository systemTransactionRepository;
    private final SystemDashboardRepository systemDashboardRepository;


    @Scheduled(cron = "0 5 0 * * ?")
    @Transactional
    public void updateShopDashboardDaily() {

        log.info("[DashboardTask] Bắt đầu tổng hợp dữ liệu Dashboard Daily cho Shop...");

        Integer year = YearMonth.now().getYear();
        Month month = YearMonth.now().getMonth();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(23, 59, 59, 999999999);
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59, 999999999);

        shopRepository.findAll().forEach(shop -> {
            ShopDashboard currentShopDashboard = shopDashboardRepository.findByShopIdAndYearAndMonth(shop.getId(), year, month).orElse(null);
            Long totalRevenue = orderRepository.calculateTotalRevenueByShop(shop.getId(), OrderStatus.PAID, startOfMonth, endOfMonth);
            Integer totalOrders = orderRepository.countOrdersByShop(shop.getId(), startOfMonth, endOfMonth, OrderStatus.PAID);
            Integer totalProducts = productRepository.countByShopId(shop.getId());
            Integer newCustomers = customerService.countNewCustomers(shop.getId(), startOfMonth, endOfMonth);
            Integer returningCustomers = customerService.countReturningCustomers(shop.getId(), startOfMonth, endOfMonth);
            Integer totalOfflineOrders = orderRepository.countAllByOrderStatusAndOrderTypeAndShopIdAndCreatedAtBetween(OrderStatus.PAID, OrderType.OFFLINE, shop.getId(), startOfMonth, endOfMonth);
            Integer totalOnlineOrders = orderRepository.countAllByOrderStatusAndOrderTypeAndShopIdAndCreatedAtBetween(OrderStatus.PAID, OrderType.ONLINE, shop.getId(), startOfMonth, endOfMonth);

            if (totalRevenue == null) {
                totalRevenue = 0l;
            }

            if (currentShopDashboard == null) { // create

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

    @Scheduled(cron = "0 5 0 * * ?")
    @Transactional
    public void updateSystemDashboardDaily() {

        log.info("[DashboardTask] Bắt đầu tổng hợp dữ liệu Dashboard Daily cho System...");

        Integer year = YearMonth.now().getYear();
        Month month = YearMonth.now().getMonth();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(23, 59, 59, 999999999);
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59, 999999999);
        SystemDashboard systemDashboard = systemDashboardRepository.findByYearAndMonth(year, month).orElse(null);

        Long totalRevenue = billingInvoiceRepository.sumAmountByStatusAndDateRange(InvoiceEnum.PAID, startOfMonth, endOfMonth);
        Integer totalSubscriptions = shopSubscriptionRepository.countBySubscriptionPlanStatusAndCreatedAtBetween(SubscriptionPlanEnum.ACTIVE, startOfMonth, endOfMonth);
        Integer newShops = shopRepository.countNewShops(startOfMonth, endOfMonth);
        Integer returningShops = shopSubscriptionRepository.countReturningShop(startOfMonth, endOfMonth);
        Long totalExpenses = systemTransactionRepository.sumAmountByCreatedAtBetween(startOfMonth, endOfMonth);

        if (totalRevenue == null) {
            totalRevenue = 0l;
        }

        if (totalExpenses == null) {
            totalExpenses = 0l;
        }

        if (systemDashboard == null) { // create

            systemDashboard = SystemDashboard.builder()
                    .year(year)
                    .month(month)
                    .totalRevenue(totalRevenue)
                    .totalSubscriptions(totalSubscriptions)
                    .newShops(newShops)
                    .returningShops(returningShops)
                    .totalExpenses(totalExpenses)
                    .build();

            systemDashboardRepository.save(systemDashboard);
        } else { // update

            systemDashboard.setTotalRevenue((totalRevenue != null) ? totalRevenue : 0);
            systemDashboard.setTotalSubscriptions((totalSubscriptions != null) ? totalSubscriptions : 0);
            systemDashboard.setNewShops((newShops != null) ? newShops : 0);
            systemDashboard.setReturningShops((returningShops != null) ? returningShops : 0);
            systemDashboard.setTotalExpenses((totalExpenses != null) ? totalExpenses : 0);

            systemDashboardRepository.save(systemDashboard);
        }

    }
}
