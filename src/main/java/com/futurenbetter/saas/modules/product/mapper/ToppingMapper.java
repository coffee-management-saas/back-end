package com.futurenbetter.saas.modules.product.mapper;

import com.futurenbetter.saas.modules.product.dto.request.ToppingRequest;
import com.futurenbetter.saas.modules.product.dto.response.ToppingResponse;
import com.futurenbetter.saas.modules.product.entity.Topping;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ToppingMapper {

    ToppingResponse toResponse(Topping entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Topping toEntity(ToppingRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    void updateFromRequest(@MappingTarget Topping entity, ToppingRequest request);
}