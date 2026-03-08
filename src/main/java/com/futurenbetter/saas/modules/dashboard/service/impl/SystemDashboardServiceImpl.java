package com.futurenbetter.saas.modules.dashboard.service.impl;

import com.futurenbetter.saas.modules.dashboard.dto.response.SystemDashboardResponse;
import com.futurenbetter.saas.modules.dashboard.mapper.SystemDashboardMapper;
import com.futurenbetter.saas.modules.dashboard.repository.SystemDashboardRepository;
import com.futurenbetter.saas.modules.dashboard.service.inter.SystemDashboardService;
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
