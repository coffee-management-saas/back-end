package com.futurenbetter.saas.modules.order.service;

import com.futurenbetter.saas.modules.order.dto.filter.OrderFilter;
import com.futurenbetter.saas.modules.order.dto.request.OrderRequest;
import com.futurenbetter.saas.modules.order.dto.response.OrderResponse;
import com.futurenbetter.saas.modules.order.entity.Order;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface OrderService {

    OrderResponse createOrder(OrderRequest orderRequest);
    void handleMomoOrderIpn(Map<String, String> payload);
    Page<OrderResponse> getOrderHistory(OrderFilter filter);
}
