package com.futurenbetter.saas.modules.order.repository;

import com.futurenbetter.saas.modules.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    @Query("SELECT SUM(o.paidPrice) FROM Order o WHERE o.shop.id = :shopId AND o.orderStatus = 'COMPLETED' AND o.createdAt >= :fromDate AND o.createdAt <= :toDate")
    Long calculateTotalRevenueByShop(@Param("shopId") Long shopId, @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId AND o.createdAt >= :fromDate AND o.createdAt <= :toDate")
    Integer countOrdersByShop(@Param("shopId") Long shopId, @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
}
