package com.futurenbetter.saas.modules.dashboard.mapper;

import com.futurenbetter.saas.modules.dashboard.dto.response.SystemDashboardResponse;
import com.futurenbetter.saas.modules.dashboard.entity.SystemDashboard;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SystemDashboardMapper {

    SystemDashboardResponse toResponse(SystemDashboard systemDashboard);
}
