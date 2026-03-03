package com.futurenbetter.saas.modules.dashboard.mapper;

import com.futurenbetter.saas.modules.dashboard.dto.response.DashboardResponse;
import com.futurenbetter.saas.modules.dashboard.entity.Dashboard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DashboardMapper {
    @Mapping(target = "shopId", source = "shop.id")
    DashboardResponse toResponse(Dashboard dashboard);
}
