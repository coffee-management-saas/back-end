package com.futurenbetter.saas.modules.dashboard.repository;

import com.futurenbetter.saas.modules.dashboard.entity.SystemMonthlyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SystemMonthlyReportRepository extends JpaRepository<SystemMonthlyReport, Long> {

    @Query("SELECT SUM(r.totalRevenue) FROM SystemMonthlyReport r WHERE " +
            "(r.reportYear > :startYear OR (r.reportYear = :startYear AND r.reportMonth >= :startMonth)) AND " +
            "(r.reportYear < :endYear OR (r.reportYear = :endYear AND r.reportMonth <= :endMonth))")
    Long sumRevenue(@Param("startMonth") int startMonth, @Param("startYear") int startYear,
                    @Param("endMonth") int endMonth, @Param("endYear") int endYear);

    @Query("SELECT SUM(r.newShopsCount) FROM SystemMonthlyReport r WHERE " +
            "(r.reportYear > :startYear OR (r.reportYear = :startYear AND r.reportMonth >= :startMonth)) AND " +
            "(r.reportYear < :endYear OR (r.reportYear = :endYear AND r.reportMonth <= :endMonth))")
    Integer sumNewShops(@Param("startMonth") int startMonth, @Param("startYear") int startYear,
                        @Param("endMonth") int endMonth, @Param("endYear") int endYear);
}
