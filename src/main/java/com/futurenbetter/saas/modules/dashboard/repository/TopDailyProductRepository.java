package com.futurenbetter.saas.modules.dashboard.repository;

import com.futurenbetter.saas.modules.dashboard.entity.TopDailyProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

import java.util.List;

public interface TopDailyProductRepository extends JpaRepository<TopDailyProduct,Long> {

    List<TopDailyProduct> findAllByShopIdAndReportDateBetween(Long shopId, LocalDate startDate, LocalDate endDate);

}
