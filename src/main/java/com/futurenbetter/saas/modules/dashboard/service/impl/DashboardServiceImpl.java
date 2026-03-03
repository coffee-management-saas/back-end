package com.futurenbetter.saas.modules.dashboard.service.impl;

import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.dashboard.dto.response.DashboardResponse;
import com.futurenbetter.saas.modules.dashboard.mapper.DashboardMapper;
import com.futurenbetter.saas.modules.dashboard.repository.DashboardRepository;
import com.futurenbetter.saas.modules.dashboard.service.inter.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final DashboardMapper dashboardMapper;

    @Override
    public List<DashboardResponse> getAll(int year) {
        Long shopId = SecurityUtils.getCurrentShopId();
        return dashboardRepository.findByShopIdAndYear(shopId, year).stream()
                .map(dashboardMapper::toResponse)
                .toList();
    }
}
