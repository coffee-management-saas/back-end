package com.futurenbetter.saas.modules.auth.dto.request;

import com.futurenbetter.saas.modules.employee.enums.EmployeeType;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopEmployeeRegistrationRequest extends SystemAdminRegistrationRequest{
    private EmployeeType employeeType;
    private Double hourlyWage;
    private Double weeklyHourLimit;
}
