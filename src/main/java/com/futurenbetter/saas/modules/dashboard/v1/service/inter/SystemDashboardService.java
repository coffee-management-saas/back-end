package com.futurenbetter.saas.modules.dashboard.v1.service.inter;


import com.futurenbetter.saas.modules.dashboard.v1.dto.response.SystemDashboardResponse;

import java.util.List;

public interface SystemDashboardService {
    List<SystemDashboardResponse> getAll(int year);

}
