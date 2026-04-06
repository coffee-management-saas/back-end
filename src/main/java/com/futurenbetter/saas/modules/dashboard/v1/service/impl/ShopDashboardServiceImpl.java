package com.futurenbetter.saas.modules.dashboard.v1.service.impl;

import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.dashboard.v1.dto.projection.BestSellerProjection;
import com.futurenbetter.saas.modules.dashboard.v1.dto.response.*;
import com.futurenbetter.saas.modules.dashboard.v1.dto.response.ShopDashboardResponse;
import com.futurenbetter.saas.modules.dashboard.v1.mapper.ShopDashboardMapper;
import com.futurenbetter.saas.modules.dashboard.v1.repository.ShopDashboardRepository;
import com.futurenbetter.saas.modules.dashboard.v1.service.inter.ShopDashboardService;
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
    private final ShopDashboardRepository shopDashboardRepository;
    private final ShopDashboardMapper shopDashboardMapper;


    @Override
    public List<ShopDashboardResponse> getAll(int year) {
        Long shopId = SecurityUtils.getCurrentShopId();
        return shopDashboardRepository.findByShopIdAndYear(shopId, year).stream()
                .map(shopDashboardMapper::toResponse)
                .toList();
    }


    @Cacheable(value = "bestSellerProducts", key = "#shopId", unless = "#result == null or #result.isEmpty()")
    @Override
    public List<BestSellerProjection> getBestSeller(Long shopId, int limit) {
        LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
        Pageable pageable = PageRequest.of(0, limit);

        return orderRepository.getBestSellerProducts(shopId, tenDaysAgo, pageable);
    }
}