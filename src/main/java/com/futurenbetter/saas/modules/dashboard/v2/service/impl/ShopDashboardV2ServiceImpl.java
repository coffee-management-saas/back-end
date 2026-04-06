package com.futurenbetter.saas.modules.dashboard.v2.service.impl;

import com.futurenbetter.saas.common.exception.BadRequestException;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.dashboard.v2.dto.response.ShopDashboardResponseV2;
import com.futurenbetter.saas.modules.dashboard.v2.entity.ShopReportV2;
import com.futurenbetter.saas.modules.dashboard.v2.enums.ReportType;
import com.futurenbetter.saas.modules.dashboard.v2.repository.ShopReportV2Repository;
import com.futurenbetter.saas.modules.dashboard.v2.service.ShopDashboardV2Service;
import com.futurenbetter.saas.modules.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopDashboardV2ServiceImpl implements ShopDashboardV2Service {

    private final OrderRepository orderRepository;
    private final ShopReportV2Repository reportRepository;
    private final ShopRepository shopRepository;

    @Override
    public ShopDashboardResponseV2 getDashboardData(Long shopId, ReportType type, LocalDate targetDate) {
        LocalDate today = LocalDate.now();

        switch (type) {
            case DAY:
                if (targetDate.equals(today)) {
                    return getRealtimeDailyStats(shopId, targetDate);
                }
                return getHistoricalReport(shopId, ReportType.DAY, targetDate);

            case WEEK:
                // Lấy dữ liệu của 7 ngày trong tuần đó từ bảng Report DAY
                return getWeeklyChartData(shopId, targetDate);

            case MONTH:
                // Lấy dữ liệu của các ngày trong tháng từ bảng Report DAY
                return getMonthlyChartData(shopId, targetDate);

            case YEAR:
                // Lấy 12 tháng từ bảng Report MONTH
                return getYearlyChartData(shopId, targetDate);

            default:
                throw new BadRequestException("Loại báo cáo không hợp lệ");
        }
    }

    // Lấy doanh thu Real-time từ bảng Order
    private ShopDashboardResponseV2 getRealtimeDailyStats(Long shopId, LocalDate date) {
        // Query SUM(totalPrice) WHERE shopId = :shopId AND createdAt = :date
        Double revenue = orderRepository.sumRevenueByShopAndDate(shopId, date.atStartOfDay(), date.atTime(23,59,59));
        Long orders = orderRepository.countOrdersByShopAndDate(shopId, date.atStartOfDay(), date.atTime(23,59,59));

        // Chart theo giờ (ví dụ lấy từ 1 câu query Group By Hour)
        List<ShopDashboardResponseV2.ChartDataPoint> points = orderRepository.getHourlyChartDataRaw(shopId, date.atStartOfDay(), date.atTime(23,59,59));

        return ShopDashboardResponseV2.builder()
                .period("Hôm nay")
                .summaryRevenue(revenue != null ? revenue : 0.0)
                .summaryOrders(orders)
                .chartData(points)
                .build();
    }

    @Override
    public ShopDashboardResponseV2 getHistoricalReport(Long shopId, ReportType type, LocalDate targetDate) {
        return reportRepository.findByShopIdAndReportTypeAndReportDate(shopId, type, targetDate)
                .map(r -> ShopDashboardResponseV2.builder()
                        .period(targetDate.toString())
                        .summaryRevenue(r.getTotalRevenue())
                        .summaryOrders(r.getTotalOrders())
                        .chartData(Collections.emptyList()) // Ngày đơn lẻ không cần chart con
                        .build())
                .orElse(new ShopDashboardResponseV2());
    }

    @Override
    public ShopDashboardResponseV2 getWeeklyChartData(Long shopId, LocalDate targetDate) {
        // Xác định ngày bắt đầu (Thứ 2) và kết thúc (CN) của tuần chứa targetDate
        LocalDate startOfWeek = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<ShopReportV2> dailyReports = reportRepository
                .findByShopIdAndReportTypeAndReportDateBetweenOrderByReportDateAsc(
                        shopId, ReportType.DAY, startOfWeek, endOfWeek);

        List<ShopDashboardResponseV2.ChartDataPoint> points = dailyReports.stream()
                .map(r -> new ShopDashboardResponseV2.ChartDataPoint(r.getReportDate().getDayOfWeek().name(), r.getTotalRevenue(), r.getTotalOrders()))
                .collect(Collectors.toList());

        return ShopDashboardResponseV2.builder()
                .period("Tuần " + targetDate.get(WeekFields.of(Locale.getDefault()).weekOfYear()))
                .summaryRevenue(points.stream().mapToDouble(ShopDashboardResponseV2.ChartDataPoint::getRevenue).sum())
                .summaryOrders(points.stream().mapToLong(ShopDashboardResponseV2.ChartDataPoint::getOrderCount).sum())
                .chartData(points)
                .build();
    }

    @Override
    public ShopDashboardResponseV2 getMonthlyChartData(Long shopId, LocalDate targetDate) {
        LocalDate startOfMonth = targetDate.withDayOfMonth(1);
        LocalDate endOfMonth = targetDate.with(TemporalAdjusters.lastDayOfMonth());

        List<ShopReportV2> dailyReports = reportRepository
                .findByShopIdAndReportTypeAndReportDateBetweenOrderByReportDateAsc(
                        shopId, ReportType.DAY, startOfMonth, endOfMonth);

        List<ShopDashboardResponseV2.ChartDataPoint> points = dailyReports.stream()
                .map(r -> new ShopDashboardResponseV2.ChartDataPoint("Ngày " + r.getReportDate().getDayOfMonth(), r.getTotalRevenue(), r.getTotalOrders()))
                .collect(Collectors.toList());

        return ShopDashboardResponseV2.builder()
                .period("Tháng " + targetDate.getMonthValue() + "/" + targetDate.getYear())
                .summaryRevenue(points.stream().mapToDouble(ShopDashboardResponseV2.ChartDataPoint::getRevenue).sum())
                .summaryOrders(points.stream().mapToLong(ShopDashboardResponseV2.ChartDataPoint::getOrderCount).sum())
                .chartData(points)
                .build();
    }

    @Override
    public ShopDashboardResponseV2 getYearlyChartData(Long shopId, LocalDate targetDate) {
        // Lấy 12 bản ghi loại MONTH của năm đó
        List<ShopReportV2> monthlyReports = reportRepository
                .findByShopIdAndReportTypeAndYearOrderByMonthAsc(shopId, ReportType.MONTH, targetDate.getYear());

        List<ShopDashboardResponseV2.ChartDataPoint> points = monthlyReports.stream()
                .map(r -> new ShopDashboardResponseV2.ChartDataPoint("Tháng " + r.getMonth(), r.getTotalRevenue(), r.getTotalOrders()))
                .collect(Collectors.toList());

        return ShopDashboardResponseV2.builder()
                .period("Năm " + targetDate.getYear())
                .summaryRevenue(points.stream().mapToDouble(ShopDashboardResponseV2.ChartDataPoint::getRevenue).sum())
                .summaryOrders(points.stream().mapToLong(ShopDashboardResponseV2.ChartDataPoint::getOrderCount).sum())
                .chartData(points)
                .build();
    }

    @Override
    @Transactional
    public void aggregateShopData(Long shopId, LocalDate targetDate) {
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.atTime(23, 59, 59);

        Double revenue = orderRepository.sumRevenueByShopAndDate(shopId, start, end);
        Long orders = orderRepository.countOrdersByShopAndDate(shopId, start, end);

        if (orders != null && orders > 0) {
            // Lưu hoặc cập nhật báo cáo ngày (DAY)
            ShopReportV2 dailyReport = reportRepository
                    .findByShopIdAndReportTypeAndReportDate(shopId, ReportType.DAY, targetDate)
                    .orElse(ShopReportV2.builder()
                            .shopId(shopId)
                            .reportType(ReportType.DAY)
                            .reportDate(targetDate)
                            .createdAt(LocalDateTime.now())
                            .build());

            dailyReport.setTotalRevenue(revenue != null ? revenue : 0.0);
            dailyReport.setTotalOrders(orders);
            dailyReport.setMonth(targetDate.getMonthValue());
            dailyReport.setYear(targetDate.getYear());
            dailyReport.setUpdatedAt(LocalDateTime.now());
            reportRepository.save(dailyReport);

            // Cập nhật các bảng tổng hợp (WEEK, MONTH, YEAR)
            // Lưu ý: Trong hàm này nên dùng logic "tính toán lại" thay vì "cộng dồn"
            // để đảm bảo nếu chạy trigger nhiều lần dữ liệu không bị sai lệch.
            refreshAggregatedReports(shopId, targetDate);
        }
    }

    @Override
    @Transactional
    public void aggregateAllShops(LocalDate targetDate) {
        shopRepository.findAll().forEach(shop -> aggregateShopData(shop.getId(), targetDate));
    }

    protected void refreshAggregatedReports(Long shopId, LocalDate date) {
        int year = date.getYear();
        int monthValue = date.getMonthValue();
        // Sử dụng chuẩn ISO-8601 để xác định số tuần
        int weekOfYear = date.get(WeekFields.of(Locale.getDefault()).weekOfYear());

        // --- 1. CẬP NHẬT BÁO CÁO TUẦN (WEEK) ---
        // Xác định Thứ 2 và Chủ Nhật của tuần chứa 'date'
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<ShopReportV2> daysInWeek = reportRepository
                .findByShopIdAndReportTypeAndReportDateBetweenOrderByReportDateAsc(
                        shopId, ReportType.DAY, startOfWeek, endOfWeek);

        updateOrSaveAggregate(shopId, ReportType.WEEK, year, null, weekOfYear, daysInWeek);

        // --- 2. CẬP NHẬT BÁO CÁO THÁNG (MONTH) ---
        // Xác định ngày đầu và ngày cuối tháng
        LocalDate startOfMonth = date.withDayOfMonth(1);
        LocalDate endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());

        List<ShopReportV2> daysInMonth = reportRepository
                .findByShopIdAndReportTypeAndReportDateBetweenOrderByReportDateAsc(
                        shopId, ReportType.DAY, startOfMonth, endOfMonth);

        updateOrSaveAggregate(shopId, ReportType.MONTH, year, monthValue, null, daysInMonth);

        // --- 3. CẬP NHẬT BÁO CÁO NĂM (YEAR) ---
        // Sau khi bản ghi MONTH đã được update ở trên, ta lấy 12 tháng của năm để tính Năm
        List<ShopReportV2> monthsInYear = reportRepository
                .findByShopIdAndReportTypeAndYearOrderByMonthAsc(shopId, ReportType.MONTH, year);

        updateOrSaveAggregate(shopId, ReportType.YEAR, year, null, null, monthsInYear);
    }

    /**
     * Hàm Helper thực hiện logic Upsert (Update hoặc Insert) dữ liệu tổng hợp
     */
    private void updateOrSaveAggregate(Long shopId, ReportType type, int year, Integer month, Integer week, List<ShopReportV2> sourceData) {
        if (sourceData == null || sourceData.isEmpty()) {
            return;
        }

        // Tính tổng Doanh thu và Đơn hàng từ danh sách nguồn
        double totalRevenue = sourceData.stream().mapToDouble(ShopReportV2::getTotalRevenue).sum();
        long totalOrders = sourceData.stream().mapToLong(ShopReportV2::getTotalOrders).sum();

        Optional<ShopReportV2> existingReport;

        // Tìm bản ghi hiện có dựa trên loại báo cáo
        switch (type) {
            case WEEK:
                existingReport = reportRepository.findByShopIdAndReportTypeAndWeekNumberAndYear(shopId, type, week, year);
                break;
            case MONTH:
                existingReport = reportRepository.findByShopIdAndReportTypeAndMonthAndYear(shopId, type, month, year);
                break;
            case YEAR:
                existingReport = reportRepository.findByShopIdAndReportTypeAndYear(shopId, type, year);
                break;
            default:
                return;
        }

        ShopReportV2 report = existingReport.orElseGet(() -> ShopReportV2.builder()
                .shopId(shopId)
                .reportType(type)
                .year(year)
                .month(month)
                .weekNumber(week)
                .createdAt(LocalDateTime.now())
                .build());

        report.setTotalRevenue(totalRevenue);
        report.setTotalOrders(totalOrders);
        report.setUpdatedAt(LocalDateTime.now());

        reportRepository.save(report);
    }
}
