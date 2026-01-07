package com.futurenbetter.saas.modules.inventory.mapper;

import com.futurenbetter.saas.modules.inventory.dto.request.RecipeRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.RecipeResponse;
import com.futurenbetter.saas.modules.inventory.entity.Recipe;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RecipeMapper {

    @Mapping(source = "rawIngredient.id", target = "ingredientId")
    @Mapping(source = "rawIngredient.name", target = "ingredientName")
    @Mapping(source = "rawIngredient.baseUnit", target = "unitName")
    RecipeResponse toResponse(Recipe entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "rawIngredient", ignore = true)
    @Mapping(target = "status", ignore = true)
    Recipe toEntity(RecipeRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "rawIngredient", ignore = true)
    void updateFromRequest(@MappingTarget Recipe entity, RecipeRequest request);
}