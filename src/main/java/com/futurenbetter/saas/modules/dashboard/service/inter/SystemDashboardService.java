package com.futurenbetter.saas.modules.dashboard.service.inter;

import com.futurenbetter.saas.modules.dashboard.dto.filter.DashboardFilter;
import com.futurenbetter.saas.modules.dashboard.dto.response.SystemDashboardResponse;

public interface SystemDashboardService {
    SystemDashboardResponse getOverview(DashboardFilter filter);
}
