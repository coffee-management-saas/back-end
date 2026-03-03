package com.futurenbetter.saas.modules.dashboard.mapper;

import com.futurenbetter.saas.modules.dashboard.dto.response.MonthlyProductSoldResponse;
import com.futurenbetter.saas.modules.dashboard.entity.MonthlyProductSold;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MonthlyProductSoldMapper {

    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "quantitySold", source = "quantitySold")
    @Mapping(target = "shopId", source = "shop.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "month", source = "month")
    @Mapping(target = "year", source = "year")
    MonthlyProductSoldResponse toResponse(MonthlyProductSold monthlyProductSold);
}
