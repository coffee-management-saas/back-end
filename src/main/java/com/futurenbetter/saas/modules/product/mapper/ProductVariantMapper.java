package com.futurenbetter.saas.modules.product.mapper;

import com.futurenbetter.saas.modules.product.dto.request.ProductVariantRequest;
import com.futurenbetter.saas.modules.product.dto.response.ProductVariantResponse;
import com.futurenbetter.saas.modules.product.entity.ProductVariant;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductVariantMapper {

    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "size.name", target = "sizeName")
    @Mapping(source = "size.code", target = "sizeCode")
    ProductVariantResponse toResponse(ProductVariant entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "size", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductVariant toEntity(ProductVariantRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "size", ignore = true)
    void updateFromRequest(@MappingTarget ProductVariant entity, ProductVariantRequest request);
}