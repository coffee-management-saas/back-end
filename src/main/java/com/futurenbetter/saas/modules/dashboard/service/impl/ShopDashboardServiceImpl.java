package com.futurenbetter.saas.modules.dashboard.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.dashboard.dto.filter.DashboardFilter;
import com.futurenbetter.saas.modules.dashboard.dto.projection.TopProductProjection;
import com.futurenbetter.saas.modules.dashboard.dto.response.*;
import com.futurenbetter.saas.modules.dashboard.entity.ShopDailyReport;
import com.futurenbetter.saas.modules.dashboard.entity.TopDailyProduct;
import com.futurenbetter.saas.modules.dashboard.mapper.TopDailyProductMapper;
import com.futurenbetter.saas.modules.dashboard.repository.ShopDailyReportRepository;
import com.futurenbetter.saas.modules.dashboard.repository.TopDailyProductRepository;
import com.futurenbetter.saas.modules.dashboard.service.inter.ShopDashboardService;
import com.futurenbetter.saas.modules.order.repository.OrderRepository;
import com.futurenbetter.saas.modules.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class ShopDashboardServiceImpl implements ShopDashboardService {

    private final OrderRepository orderRepository;
    private final ShopDailyReportRepository shopDailyReportRepository;
    private final TopDailyProductRepository topDailyProductRepository; // THÊM REPO NÀY
    private final TopDailyProductMapper topDailyProductMapper;
    private final ShopRepository shopRepository;

    @Override
    public List<ShopDashboardDailyResponse> getDaily(DashboardFilter filter) {
        Long shopId = SecurityUtils.getCurrentShopId();

        // 1. Khởi tạo mốc thời gian
        LocalDate fromDate = filter.getFromDate() != null ? filter.getFromDate() : LocalDate.now().minusDays(30);
        LocalDate toDate = filter.getToDate() != null ? filter.getToDate() : LocalDate.now();
        LocalDate today = LocalDate.now();

        int limit = filter.getTopProductsLimit() != null ? filter.getTopProductsLimit() : 5;
        Pageable topProductsPageable = PageRequest.of(0, limit);

        // 2. LẤY DỮ LIỆU QUÁ KHỨ 1 LẦN (Tránh lỗi N+1 Query)
        LocalDate endOfPastDate = toDate.isBefore(today) ? toDate : today.minusDays(1);

        Map<LocalDate, ShopDailyReport> pastReportsMap = Map.of();
        Map<LocalDate, List<TopDailyProduct>> pastTopProductsMap = Map.of();

        if (!fromDate.isAfter(endOfPastDate)) {
            // Lấy toàn bộ report trong khoảng thời gian rồi đẩy vào Map với Key là Ngày (LocalDate)
            pastReportsMap = shopDailyReportRepository
                    .findAllByShopIdAndReportDateBetween(shopId, fromDate, endOfPastDate)
                    .stream()
                    .collect(Collectors.toMap(ShopDailyReport::getReportDate, r -> r));

            // Lấy toàn bộ Top Product quá khứ rồi Group By theo Ngày
            pastTopProductsMap = topDailyProductRepository
                    .findAllByShopIdAndReportDateBetween(shopId, fromDate, endOfPastDate)
                    .stream()
                    .collect(Collectors.groupingBy(TopDailyProduct::getReportDate));
        }

        // 3. DUYỆT TỪNG NGÀY ĐỂ TẠO RESPONSE (Cho biểu đồ)
        List<ShopDashboardDailyResponse> dailyResponses = new ArrayList<>();
        LocalDate currentDate = fromDate;

        while (!currentDate.isAfter(toDate)) {

            // TH 1: NẾU LÀ HÔM NAY -> TÍNH TOÁN REALTIME TỪ BẢNG ORDER
            if (currentDate.isEqual(today)) {
                LocalDateTime startOfToday = today.atStartOfDay();
                LocalDateTime endOfToday = today.atTime(23, 59, 59, 999999999);

                Long liveRev = orderRepository.calculateTotalRevenueByShop(shopId, startOfToday, endOfToday);
                Integer liveOrd = orderRepository.countOrdersByShop(shopId, startOfToday, endOfToday);
                Integer liveOrdWithPromotion = orderRepository.countOdersByShopIdAndHasPromotionIsTrue(shopId, startOfToday, endOfToday);

                // Tránh lỗi chia cho 0
                double usingPromoRate = (liveOrd != null && liveOrd > 0)
                        ? (liveOrdWithPromotion != null ? liveOrdWithPromotion : 0) * 100.0 / liveOrd
                        : 0.0;

                // Lấy Top Món hôm nay
                List<TopProductProjection> topProjections = orderRepository.findTopSellingProducts(shopId, startOfToday, endOfToday, topProductsPageable);
                List<TopDailyProductResponse> topProductsToday = topProjections.stream()
                        .map(proj -> TopDailyProductResponse.builder()
                                .productName(proj.getProductName())
                                .quantitySold(proj.getTotalQuantity().intValue())
                                .build())
                        .collect(Collectors.toList());

                dailyResponses.add(ShopDashboardDailyResponse.builder()
                        .reportDate(today)
                        .totalRevenue(liveRev != null ? liveRev : 0L)
                        .totalOrders(liveOrd != null ? liveOrd : 0)
                        .usingPromotionRate(usingPromoRate)
                        .topProducts(topProductsToday)
                        .build());
            }
            // TH 2: NẾU LÀ QUÁ KHỨ -> LẤY TỪ MAP ĐÃ LOAD Ở BƯỚC 2
            else {
                ShopDailyReport report = pastReportsMap.get(currentDate);
                List<TopDailyProduct> pastTopProds = pastTopProductsMap.getOrDefault(currentDate, new ArrayList<>());

                // Giới hạn số lượng top theo tham số filter
                List<TopDailyProductResponse> topProductResponses = pastTopProds.stream()
                        .limit(limit)
                        .map(topDailyProductMapper::toResponse)
                        .collect(Collectors.toList());

                dailyResponses.add(ShopDashboardDailyResponse.builder()
                        .reportDate(currentDate)
                        .totalRevenue(report != null ? report.getTotalRevenue() : 0L)
                        .totalOrders(report != null ? report.getTotalOrders() : 0)
                        .usingPromotionRate(report != null ? report.getUsingVouchersPercentage() : 0.0) // Giả định entity có trường này
                        .topProducts(topProductResponses)
                        .build());
            }

            // Tiến lên ngày tiếp theo
            currentDate = currentDate.plusDays(1);
        }

        return dailyResponses;
    }

    @Override
    public ShopDashboardResponse getShopDashboard(DashboardFilter filter) {
        Long shopId = SecurityUtils.getCurrentShopId();
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new BusinessException("Không tìm thấy Shop"));

        LocalDate fromDate = filter.getFromDate() != null ? filter.getFromDate() : LocalDate.now().minusDays(30);
        LocalDate toDate = filter.getToDate() != null ? filter.getToDate() : LocalDate.now();
        LocalDate today = LocalDate.now();
        int limit = filter.getTopProductsLimit() != null ? filter.getTopProductsLimit() : 5;

        // =========================================================================
        // BƯỚC 1: XÁC ĐỊNH KHOẢNG THỜI GIAN BAO PHỦ RỘNG NHẤT (WIDEST RANGE)
        // =========================================================================
        // Tìm Thứ 2 của tuần đầu tiên và Chủ nhật của tuần cuối cùng
        LocalDate startOfFirstWeek = fromDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfLastWeek = toDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // Tìm Mùng 1 của tháng đầu tiên và Ngày cuối cùng của tháng cuối cùng
        LocalDate startOfFirstMonth = fromDate.withDayOfMonth(1);
        LocalDate endOfLastMonth = toDate.with(TemporalAdjusters.lastDayOfMonth());

        // Khoảng thời gian thực tế cần fetch DB
        LocalDate fetchStartDate = startOfFirstWeek.isBefore(startOfFirstMonth) ? startOfFirstWeek : startOfFirstMonth;
        LocalDate fetchEndDate = endOfLastWeek.isAfter(endOfLastMonth) ? endOfLastWeek : endOfLastMonth;

        // =========================================================================
        // BƯỚC 2: KÉO TOÀN BỘ DỮ LIỆU VÀO RAM (Tránh N+1 Query)
        // =========================================================================
        Map<LocalDate, ShopDailyReport> dailyReportMap = new HashMap<>();
        Map<LocalDate, List<TopDailyProduct>> dailyTopProductMap = new HashMap<>();

        // 2.1 Lấy dữ liệu quá khứ
        LocalDate endOfPastDate = fetchEndDate.isBefore(today) ? fetchEndDate : today.minusDays(1);
        if (!fetchStartDate.isAfter(endOfPastDate)) {
            List<ShopDailyReport> pastReports = shopDailyReportRepository.findAllByShopIdAndReportDateBetween(shopId, fetchStartDate, endOfPastDate);
            pastReports.forEach(r -> dailyReportMap.put(r.getReportDate(), r));

            List<TopDailyProduct> pastTopProds = topDailyProductRepository.findAllByShopIdAndReportDateBetween(shopId, fetchStartDate, endOfPastDate);
            dailyTopProductMap = pastTopProds.stream().collect(Collectors.groupingBy(TopDailyProduct::getReportDate));
        }

        // 2.2 Lấy dữ liệu hôm nay (Nếu hôm nay nằm trong khoảng cần lấy)
        if (!fetchEndDate.isBefore(today) && !fetchStartDate.isAfter(today)) {
            LocalDateTime startOfToday = today.atStartOfDay();
            LocalDateTime endOfToday = today.atTime(23, 59, 59, 999999999);

            Long liveRev = orderRepository.calculateTotalRevenueByShop(shopId, startOfToday, endOfToday);
            Integer liveOrd = orderRepository.countOrdersByShop(shopId, startOfToday, endOfToday);
            Integer liveOrdWithPromotion = orderRepository.countOdersByShopIdAndHasPromotionIsTrue(shopId, startOfToday, endOfToday);

            double liveUsingPromoRate = (liveOrd != null && liveOrd > 0) ? (liveOrdWithPromotion != null ? liveOrdWithPromotion : 0) * 100.0 / liveOrd : 0.0;

            ShopDailyReport liveReport = ShopDailyReport.builder()
                    .totalRevenue(liveRev != null ? liveRev : 0L)
                    .totalOrders(liveOrd != null ? liveOrd : 0)
                    // Lưu ý: Tùy Entity của bạn có field nào để lưu PromoRate, tạm ví dụ:
                    // .usingPromotionRate(liveUsingPromoRate)
                    .build();
            dailyReportMap.put(today, liveReport);

            List<TopProductProjection> topLiveProds = orderRepository.findTopSellingProducts(shopId, startOfToday, endOfToday, PageRequest.of(0, limit));
            List<TopDailyProduct> liveTopProds = topLiveProds.stream().map(proj -> {
                Product p = new Product(); p.setId(proj.getProductId()); p.setName(proj.getProductName());
                return TopDailyProduct.builder().product(p).quantitySold(proj.getTotalQuantity().intValue()).build();
            }).toList();

            dailyTopProductMap.put(today, liveTopProds);
        }

        // =========================================================================
        // BƯỚC 3: TỔNG HỢP THEO TUẦN (WEEKLY REPORTS)
        // =========================================================================
        List<ShopDashboardWeeklyResponse> weeklyReports = new ArrayList<>();
        LocalDate currentWeekStart = startOfFirstWeek;

        while (!currentWeekStart.isAfter(endOfLastWeek)) {
            LocalDate currentWeekEnd = currentWeekStart.plusDays(6);

            // Hàm helper bên dưới sẽ làm nhiệm vụ gom nhóm 7 ngày lại thành 1 cục
            weeklyReports.add(aggregateWeekly(currentWeekStart, currentWeekEnd, dailyReportMap, dailyTopProductMap, limit));

            currentWeekStart = currentWeekStart.plusWeeks(1); // Nhảy sang thứ 2 tuần tiếp theo
        }

        // =========================================================================
        // BƯỚC 4: TỔNG HỢP THEO THÁNG (MONTHLY REPORTS)
        // =========================================================================
        List<ShopDashboardMonthlyResponse> monthlyReports = new ArrayList<>();
        YearMonth currentMonth = YearMonth.from(startOfFirstMonth);
        YearMonth lastMonth = YearMonth.from(endOfLastMonth);

        while (!currentMonth.isAfter(lastMonth)) {
            LocalDate monthStart = currentMonth.atDay(1);
            LocalDate monthEnd = currentMonth.atEndOfMonth();

            // Hàm helper bên dưới sẽ làm nhiệm vụ gom nhóm các ngày trong tháng
            monthlyReports.add(aggregateMonthly(monthStart, monthEnd, dailyReportMap, dailyTopProductMap, limit));

            currentMonth = currentMonth.plusMonths(1); // Nhảy sang mùng 1 tháng tiếp theo
        }

        return ShopDashboardResponse.builder()
                .weeklyReports(weeklyReports)
                .monthlyReports(monthlyReports)
                .build();
    }

    private ShopDashboardWeeklyResponse aggregateWeekly(LocalDate startOfWeek, LocalDate endOfWeek,
                                                        Map<LocalDate, ShopDailyReport> reportMap,
                                                        Map<LocalDate, List<TopDailyProduct>> productMap,
                                                        int limit) {
        long totalRev = 0L;
        int totalOrd = 0;
        // Gom mã món ăn và số lượng
        Map<Long, TopDailyProductResponse> topProductAggregator = new HashMap<>();

        for (LocalDate date = startOfWeek; !date.isAfter(endOfWeek); date = date.plusDays(1)) {
            ShopDailyReport report = reportMap.get(date);
            if (report != null) {
                totalRev += report.getTotalRevenue() != null ? report.getTotalRevenue() : 0L;
                totalOrd += report.getTotalOrders() != null ? report.getTotalOrders() : 0;
            }

            List<TopDailyProduct> prods = productMap.get(date);
            if (prods != null) {
                for (TopDailyProduct p : prods) {
                    topProductAggregator.compute(p.getProduct().getId(), (id, existing) -> {
                        if (existing == null) {
                            return TopDailyProductResponse.builder()
                                    .productName(p.getProduct().getName())
                                    .quantitySold(p.getQuantitySold())
                                    .build();
                        }
                        existing.setQuantitySold(existing.getQuantitySold() + p.getQuantitySold());
                        return existing;
                    });
                }
            }
        }

        // Sắp xếp món ăn bán chạy nhất và lấy top 'limit'
        List<TopDailyProductResponse> finalTopProducts = topProductAggregator.values().stream()
                .sorted(Comparator.comparing(TopDailyProductResponse::getQuantitySold).reversed())
                .limit(limit)
                .toList();

        return ShopDashboardWeeklyResponse.builder()
                .startOfWeek(startOfWeek)
                .totalRevenue(totalRev)
                .totalOrders(totalOrd)
                .usingPromotionRate(0.0) // Chú ý: Để tính Rate chính xác bạn cần số lượng Promo Orders. Mình để tạm 0.0
                .topProducts(finalTopProducts)
                .build();
    }

    private ShopDashboardMonthlyResponse aggregateMonthly(LocalDate startOfMonth, LocalDate endOfMonth,
                                                          Map<LocalDate, ShopDailyReport> reportMap,
                                                          Map<LocalDate, List<TopDailyProduct>> productMap,
                                                          int limit) {
        long totalRev = 0L;
        int totalOrd = 0;
        Map<Long, TopDailyProductResponse> topProductAggregator = new HashMap<>();

        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            ShopDailyReport report = reportMap.get(date);
            if (report != null) {
                totalRev += report.getTotalRevenue() != null ? report.getTotalRevenue() : 0L;
                totalOrd += report.getTotalOrders() != null ? report.getTotalOrders() : 0;
            }

            List<TopDailyProduct> prods = productMap.get(date);
            if (prods != null) {
                for (TopDailyProduct p : prods) {
                    topProductAggregator.compute(p.getProduct().getId(), (id, existing) -> {
                        if (existing == null) {
                            return TopDailyProductResponse.builder()
                                    .productName(p.getProduct().getName())
                                    .quantitySold(p.getQuantitySold())
                                    .build();
                        }
                        existing.setQuantitySold(existing.getQuantitySold() + p.getQuantitySold());
                        return existing;
                    });
                }
            }
        }

        List<TopDailyProductResponse> finalTopProducts = topProductAggregator.values().stream()
                .sorted(Comparator.comparing(TopDailyProductResponse::getQuantitySold).reversed())
                .limit(limit)
                .toList();

        return ShopDashboardMonthlyResponse.builder()
                .reportMonth(startOfMonth.getMonth())
                .totalRevenue(totalRev)
                .totalOrders(totalOrd)
                .usingPromotionRate(0.0) // Tương tự
                .topProducts(finalTopProducts)
                .build();
    }
}