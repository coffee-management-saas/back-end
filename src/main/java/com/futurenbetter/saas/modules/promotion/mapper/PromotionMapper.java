package com.futurenbetter.saas.modules.promotion.mapper;

import com.futurenbetter.saas.modules.promotion.dto.request.PromotionRequest;
import com.futurenbetter.saas.modules.promotion.dto.response.PromotionResponse;
import com.futurenbetter.saas.modules.promotion.entity.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(target = "promotionId", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "promotionTargets", ignore = true)
    @Mapping(target = "promotionUsages", ignore = true)
    Promotion toEntity(PromotionRequest request);


    @Mapping(source = "shop.id", target = "shopId")
    @Mapping(source = "createdAt", target = "createdDate")
    @Mapping(source = "updatedAt", target = "updatedDate")
    PromotionResponse toResponse(Promotion promotion);
}
