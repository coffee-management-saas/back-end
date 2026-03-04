package com.futurenbetter.saas.modules.dashboard.service.inter;


import com.futurenbetter.saas.modules.dashboard.dto.response.SystemDashboardResponse;

import java.util.List;

public interface SystemDashboardService {
    List<SystemDashboardResponse> getAll(int year);

}
