package com.futurenbetter.saas.modules.dashboard.v1.mapper;

import com.futurenbetter.saas.modules.dashboard.v1.dto.response.SystemDashboardResponse;
import com.futurenbetter.saas.modules.dashboard.v1.entity.SystemDashboard;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SystemDashboardMapper {

    SystemDashboardResponse toResponse(SystemDashboard systemDashboard);
}
