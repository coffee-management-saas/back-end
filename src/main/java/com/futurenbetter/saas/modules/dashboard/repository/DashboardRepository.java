package com.futurenbetter.saas.modules.dashboard.repository;

import com.futurenbetter.saas.modules.dashboard.entity.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Month;
import java.util.List;
import java.util.Optional;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
    Optional<Dashboard> findByShopIdAndYearAndMonth(Long shopId, Integer year, Month month);
    List<Dashboard> findByShopIdAndYear(Long shopId, Integer year);
}
