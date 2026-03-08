package com.futurenbetter.saas.modules.inventory.mapper;

import com.futurenbetter.saas.modules.inventory.dto.request.RawIngredientRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.RawIngredientResponse;
import com.futurenbetter.saas.modules.inventory.entity.RawIngredient;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RawIngredientMapper {

    @Mapping(target = "totalStockQuantity", ignore = true)
    RawIngredientResponse toResponse(RawIngredient entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "inventoryStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RawIngredient toEntity(RawIngredientRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    void updateFromRequest(@MappingTarget RawIngredient entity, RawIngredientRequest request);
}