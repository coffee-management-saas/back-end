package com.futurenbetter.saas.modules.order.repository;

import com.futurenbetter.saas.modules.auth.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
