package com.futurenbetter.saas.modules.employee.mapper;

import com.futurenbetter.saas.modules.employee.dto.request.ScheduleRequest;
import com.futurenbetter.saas.modules.employee.dto.response.ScheduleResponse;
import com.futurenbetter.saas.modules.employee.entity.Schedule;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ScheduleMapper {

    @Mapping(target = "employeeName", source = "employee.userProfile.fullname")
    @Mapping(target = "employeeType", source = "employee.employeeType")
    @Mapping(target = "employeeId", source = "employee.employeeId")
    ScheduleResponse toResponse(Schedule entity);

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "scheduleId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Schedule toEntity(ScheduleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "scheduleId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(ScheduleRequest request, @MappingTarget Schedule entity);

    @AfterMapping
    default void calculateFields(@MappingTarget Schedule entity, ScheduleRequest request) {
        if (request.getStartTime() != null) {
            entity.setDayOfWeek(request.getStartTime().getDayOfWeek());
        }
    }
}
