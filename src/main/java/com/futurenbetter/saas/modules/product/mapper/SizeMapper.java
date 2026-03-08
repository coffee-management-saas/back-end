package com.futurenbetter.saas.modules.product.mapper;

import com.futurenbetter.saas.modules.product.dto.request.SizeRequest;
import com.futurenbetter.saas.modules.product.dto.response.SizeResponse;
import com.futurenbetter.saas.modules.product.entity.Size;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SizeMapper {

    @Mapping(target = "sizeId", source = "id")
    SizeResponse toResponse(Size size); // Size entity đơn giản nên dùng trực tiếp làm response cũng được, nhưng tách DTO tốt hơn

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Size toEntity(SizeRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateFromRequest(@MappingTarget Size entity, SizeRequest request);
}