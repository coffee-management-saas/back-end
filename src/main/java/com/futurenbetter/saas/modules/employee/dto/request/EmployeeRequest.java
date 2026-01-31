package com.futurenbetter.saas.modules.employee.dto.request;

import com.futurenbetter.saas.modules.employee.enums.EmployeeStatus;
import com.futurenbetter.saas.modules.employee.enums.EmployeeType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeRequest {
    Long userProfileId;
    EmployeeType employeeType;
    Double hourlyWage;
    Double weeklyHourLimit;
}
