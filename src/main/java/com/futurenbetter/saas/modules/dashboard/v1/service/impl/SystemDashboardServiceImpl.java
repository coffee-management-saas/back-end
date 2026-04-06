package com.futurenbetter.saas.modules.dashboard.v1.service.impl;

import com.futurenbetter.saas.modules.dashboard.v1.dto.response.SystemDashboardResponse;
import com.futurenbetter.saas.modules.dashboard.v1.mapper.SystemDashboardMapper;
import com.futurenbetter.saas.modules.dashboard.v1.repository.SystemDashboardRepository;
import com.futurenbetter.saas.modules.dashboard.v1.service.inter.SystemDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemDashboardServiceImpl implements SystemDashboardService {

    private final SystemDashboardRepository systemDashboardRepository;
    private final SystemDashboardMapper systemDashboardMapper;


    @Override
    public List<SystemDashboardResponse> getAll(int year) {
        return systemDashboardRepository.findAllByYear(year).stream()
                .map(systemDashboardMapper::toResponse).toList();
    }
}
