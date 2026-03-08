package com.futurenbetter.saas.modules.employee.dto.response;

import com.futurenbetter.saas.modules.employee.enums.EmployeeType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeResponse {
    Long employeeId;
    Long shopId;
    Long userProfileId;
    EmployeeType employeeType;
    Double hourlyWage;
    Double weeklyHourLimit;
    LocalDateTime updatedAt;
}
