package com.futurenbetter.saas.modules.employee.mapper;

import com.futurenbetter.saas.modules.employee.dto.request.EmployeeRequest;
import com.futurenbetter.saas.modules.employee.dto.response.EmployeeResponse;
import com.futurenbetter.saas.modules.employee.entity.Employee;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EmployeeMapper {

    EmployeeResponse toResponse(Employee employee);

    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "userProfile", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Employee toEntity(EmployeeRequest employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "userProfile", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(EmployeeRequest employeeRequest, @MappingTarget Employee employee);
}
