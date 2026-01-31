package com.futurenbetter.saas.modules.employee.dto.response;

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
    String employeeType;
    Double hourlyWage;
    Double weeklyHourLimit;
    LocalDateTime updatedAt;
}
