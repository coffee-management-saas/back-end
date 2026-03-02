package com.futurenbetter.saas.modules.dashboard.repository;

import com.futurenbetter.saas.modules.dashboard.entity.ShopDailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ShopDailyReportRepository extends JpaRepository<ShopDailyReport, Long> {

    @Query("SELECT SUM(r.totalRevenue) FROM ShopDailyReport r WHERE r.shop.id = :shopId AND r.reportDate >= :startDate AND r.reportDate <= :endDate")
    Long sumRevenue(@Param("shopId") Long shopId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(r.totalOrders) FROM ShopDailyReport r WHERE r.shop.id = :shopId AND r.reportDate >= :startDate AND r.reportDate <= :endDate")
    Integer sumOrders(@Param("shopId") Long shopId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT AVG(r.usingVouchersPercentage) FROM ShopDailyReport r WHERE r.shop.id = :shopId AND r.reportDate >= :startDate AND r.reportDate <= :endDate")
    Double averageOrdersHasPromotion(@Param("shopId") Long shopId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    List<ShopDailyReport> findAllByShopIdAndReportDateBetween(Long shopId, LocalDate startDate, LocalDate endDate);
}
