package com.futurenbetter.saas.modules.auth.dto.request;

import com.futurenbetter.saas.modules.employee.enums.EmployeeType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopEmployeeRegistrationRequest extends SystemAdminRegistrationRequest{
    private Long shopId;
    private EmployeeType employeeType;
    private Double hourlyWage;
    private Double weeklyHourLimit;
}
