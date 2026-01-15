package com.futurenbetter.saas.modules.product.mapper;

import com.futurenbetter.saas.modules.product.dto.request.CategoryRequest;
import com.futurenbetter.saas.modules.product.dto.response.CategoryResponse;
import com.futurenbetter.saas.modules.product.entity.Category;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CategoryMapper {

    CategoryResponse toResponse(Category entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CategoryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    void updateFromRequest(@MappingTarget Category entity, CategoryRequest request);
}