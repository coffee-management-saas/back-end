package com.futurenbetter.saas.modules.dashboard.repository;

import com.futurenbetter.saas.modules.dashboard.entity.SystemDashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Month;
import java.util.List;
import java.util.Optional;

public interface SystemDashboardRepository extends JpaRepository<SystemDashboard, Long> {
    Optional<SystemDashboard> findByYearAndMonth(Integer year, Month month);
    List<SystemDashboard> findAllByYear(Integer year);
}
