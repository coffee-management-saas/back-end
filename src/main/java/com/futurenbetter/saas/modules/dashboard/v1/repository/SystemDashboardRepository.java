package com.futurenbetter.saas.modules.dashboard.v1.repository;

import com.futurenbetter.saas.modules.dashboard.v1.entity.SystemDashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Month;
import java.util.List;
import java.util.Optional;

public interface SystemDashboardRepository extends JpaRepository<SystemDashboard, Long> {
    Optional<SystemDashboard> findByYearAndMonth(Integer year, Month month);
    List<SystemDashboard> findAllByYear(Integer year);
}
