package com.futurenbetter.saas.modules.dashboard.repository;

import com.futurenbetter.saas.modules.dashboard.entity.ShopDashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Month;
import java.util.List;
import java.util.Optional;

public interface ShopDashboardRepository extends JpaRepository<ShopDashboard, Long> {
    Optional<ShopDashboard> findByShopIdAndYearAndMonth(Long shopId, Integer year, Month month);
    List<ShopDashboard> findByShopIdAndYear(Long shopId, Integer year);
}
