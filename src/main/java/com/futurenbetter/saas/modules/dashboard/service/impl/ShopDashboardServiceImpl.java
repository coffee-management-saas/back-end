package com.futurenbetter.saas.modules.dashboard.service.impl;

import com.futurenbetter.saas.modules.dashboard.dto.projection.BestSellerProjection;
import com.futurenbetter.saas.modules.dashboard.dto.response.*;
import com.futurenbetter.saas.modules.dashboard.service.inter.ShopDashboardService;
import com.futurenbetter.saas.modules.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopDashboardServiceImpl implements ShopDashboardService {

    private final OrderRepository orderRepository;


    @Cacheable(value = "bestSellerProducts", key = "#shopId", unless = "#result == null or #result.isEmpty()")
    @Override
    public List<BestSellerProjection> getBestSeller(Long shopId, int limit) {
        LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
        Pageable pageable = PageRequest.of(0, limit);

        return orderRepository.getBestSellerProducts(shopId, tenDaysAgo, pageable);
    }
}