package com.futurenbetter.saas.modules.dashboard.mapper;

import com.futurenbetter.saas.modules.dashboard.dto.response.TopDailyProductResponse;
import com.futurenbetter.saas.modules.dashboard.entity.TopDailyProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TopDailyProductMapper {
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "quantitySold", source = "quantitySold")
    TopDailyProductResponse toResponse(TopDailyProduct topDailyProduct);
}
