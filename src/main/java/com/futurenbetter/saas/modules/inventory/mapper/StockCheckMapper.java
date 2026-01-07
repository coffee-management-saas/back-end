package com.futurenbetter.saas.modules.inventory.mapper;

import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckStartRequest;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckItemRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.StockCheckSessionResponse;
import com.futurenbetter.saas.modules.inventory.dto.response.StockCheckDetailResponse;
import com.futurenbetter.saas.modules.inventory.entity.StockCheckSession;
import com.futurenbetter.saas.modules.inventory.entity.StockCheckDetail;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface StockCheckMapper {

    @Mapping(target = "createdByName", ignore = true)
    StockCheckSessionResponse toSessionResponse(StockCheckSession entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "isApproved", constant = "false")
    @Mapping(target = "status", constant = "ACTIVE") // Hoặc DRAFT
    StockCheckSession toSessionEntity(StockCheckStartRequest request);

    @Mapping(source = "ingredient.id", target = "ingredientId")
    @Mapping(source = "ingredient.name", target = "ingredientName")
    StockCheckDetailResponse toDetailResponse(StockCheckDetail entity);

    List<StockCheckDetailResponse> toDetailResponseList(List<StockCheckDetail> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "snapshotQuantity", ignore = true)
    @Mapping(target = "diffQuantity", ignore = true)
    void updateDetailFromRequest(@MappingTarget StockCheckDetail entity, StockCheckItemRequest request);
}