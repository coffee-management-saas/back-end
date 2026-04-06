package com.futurenbetter.saas.modules.dashboard.v1.mapper;

import com.futurenbetter.saas.modules.dashboard.v1.dto.response.ShopDashboardResponse;
import com.futurenbetter.saas.modules.dashboard.v1.entity.ShopDashboard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ShopDashboardMapper {
    @Mapping(target = "shopId", source = "shop.id")
    ShopDashboardResponse toResponse(ShopDashboard shopDashboard);
}
