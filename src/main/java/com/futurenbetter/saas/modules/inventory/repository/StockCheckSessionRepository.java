package com.futurenbetter.saas.modules.inventory.repository;

import com.futurenbetter.saas.modules.inventory.entity.StockCheckSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockCheckSessionRepository extends JpaRepository<StockCheckSession, Long>, JpaSpecificationExecutor<StockCheckSession> {
    Optional<StockCheckSession> findByIdAndShopId(Long id, Long shopId);
}