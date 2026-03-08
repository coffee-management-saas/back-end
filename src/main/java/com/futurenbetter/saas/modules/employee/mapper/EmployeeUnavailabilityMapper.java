package com.futurenbetter.saas.modules.employee.mapper;

import com.futurenbetter.saas.modules.employee.dto.request.EmployeeUnavailabilityRequest;
import com.futurenbetter.saas.modules.employee.dto.response.EmployeeUnavailabilityResponse;
import com.futurenbetter.saas.modules.employee.entity.EmployeeUnavailability;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EmployeeUnavailabilityMapper {

    @Mapping(target = "employeeId", source = "employee.employeeId")
    @Mapping(target = "employeeName", source = "employee.userProfile.fullname")
    EmployeeUnavailabilityResponse toResponse(EmployeeUnavailability entity);

    @Mapping(target = "employeeUnavailabilityId", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "dayOfWeek", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    EmployeeUnavailability toEntity(EmployeeUnavailabilityRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employeeUnavailabilityId", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "dayOfWeek", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateFromRequest(EmployeeUnavailabilityRequest request, @MappingTarget EmployeeUnavailability entity);

    @AfterMapping
    default void calculateDayOfWeek(EmployeeUnavailabilityRequest request, @MappingTarget EmployeeUnavailability entity) {
        if (request.getSpecificDate() != null) {
            entity.setDayOfWeek(request.getSpecificDate().getDayOfWeek());
        } else if (request.getStartTime() != null) {
            entity.setDayOfWeek(request.getStartTime().getDayOfWeek());
        }
    }
}
