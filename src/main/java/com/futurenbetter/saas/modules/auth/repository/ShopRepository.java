package com.futurenbetter.saas.modules.auth.repository;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long>, JpaSpecificationExecutor<Shop> {
    Optional<Shop> findByDomain(String domain);
    boolean existsByDomain(String domain);
    boolean existsByEmail(String email);
    @Query("SELECT COUNT(s) FROM Shop s WHERE s.createdAt >= :fromDate AND s.createdAt <= :toDate")
    Integer countNewShops(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
}
