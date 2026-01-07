package com.futurenbetter.saas.modules.inventory.mapper;

import com.futurenbetter.saas.modules.inventory.dto.response.IngredientBatchResponse;
import com.futurenbetter.saas.modules.inventory.entity.IngredientBatch;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IngredientBatchMapper {

    @Mapping(source = "rawIngredient.name", target = "ingredientName")
    IngredientBatchResponse toResponse(IngredientBatch entity);
}