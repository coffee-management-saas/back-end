package com.futurenbetter.saas.modules.employee.mapper;

import com.futurenbetter.saas.modules.employee.dto.request.ShiftTemplateRequest;
import com.futurenbetter.saas.modules.employee.dto.response.ShiftTemplateResponse;
import com.futurenbetter.saas.modules.employee.entity.ShiftTemplate;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ShiftTemplateMapper {

    ShiftTemplateResponse toResponse(ShiftTemplate shiftTemplate);

    @Mapping(target = "shiftTemplateId", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ShiftTemplate toEntity(ShiftTemplateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "shiftTemplateId", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(ShiftTemplateRequest request, @MappingTarget ShiftTemplate shiftTemplate);
}
