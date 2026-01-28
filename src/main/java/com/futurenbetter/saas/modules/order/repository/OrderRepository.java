package com.futurenbetter.saas.modules.order.repository;

import com.futurenbetter.saas.modules.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
