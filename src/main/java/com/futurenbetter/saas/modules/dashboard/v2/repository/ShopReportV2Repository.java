package com.futurenbetter.saas.modules.dashboard.v2.repository;

import com.futurenbetter.saas.modules.dashboard.v2.entity.ShopReportV2;
import com.futurenbetter.saas.modules.dashboard.v2.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShopReportV2Repository extends JpaRepository<ShopReportV2, Long> {
    Optional<ShopReportV2> findByShopIdAndReportTypeAndReportDate(Long shopId, ReportType type, LocalDate date);

    List<ShopReportV2> findByShopIdAndReportTypeAndReportDateBetweenOrderByReportDateAsc(
            Long shopId, ReportType type, LocalDate start, LocalDate end);

    List<ShopReportV2> findByShopIdAndReportTypeAndYearOrderByMonthAsc(
            Long shopId, ReportType type, Integer year);

    Optional<ShopReportV2> findByShopIdAndReportTypeAndWeekNumberAndYear(
            Long shopId, ReportType type, Integer weekNumber, Integer year);

    Optional<ShopReportV2> findByShopIdAndReportTypeAndMonthAndYear(
            Long shopId, ReportType type, Integer month, Integer year);

    Optional<ShopReportV2> findByShopIdAndReportTypeAndYear(Long shopId, ReportType type, Integer year);
}
