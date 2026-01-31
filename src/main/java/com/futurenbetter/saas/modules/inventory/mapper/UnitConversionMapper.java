package com.futurenbetter.saas.modules.inventory.mapper;

import com.futurenbetter.saas.modules.inventory.dto.request.UnitConversionRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.UnitConversionResponse;
import com.futurenbetter.saas.modules.inventory.entity.UnitConversion;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UnitConversionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "inventoryStatus", ignore = true)
    UnitConversion toEntity(UnitConversionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    void updateFromRequest(@MappingTarget UnitConversion entity, UnitConversionRequest request);

    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    UnitConversionResponse toResponse(UnitConversion entity);
}
