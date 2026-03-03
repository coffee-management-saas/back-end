package com.futurenbetter.saas.modules.dashboard.service.inter;

import com.futurenbetter.saas.modules.dashboard.dto.response.DashboardResponse;

import java.util.List;

public interface DashboardService {
    List<DashboardResponse> getAll(int year);
}
