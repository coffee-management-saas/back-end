package com.futurenbetter.saas.modules.auth.mapper;

import com.futurenbetter.saas.modules.auth.dto.request.ShopRequest;
import com.futurenbetter.saas.modules.auth.dto.response.ShopResponse;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ShopMapper {
    @Mapping(target = "status", source = "shopStatus")
    ShopResponse toResponse(Shop shop);

    Shop toEntity(ShopRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "shopStatus", source = "status")
    void updateShopFromRequest(ShopRequest request, @MappingTarget Shop shop);
}
