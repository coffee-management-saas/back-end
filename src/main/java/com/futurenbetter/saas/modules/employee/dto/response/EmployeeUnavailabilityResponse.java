package com.futurenbetter.saas.modules.employee.dto.response;

import com.futurenbetter.saas.modules.employee.enums.UnavailabilityStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeUnavailabilityResponse {
    Long unavailabilityId;
    Long employeeId;
    String employeeName;
    String reason;
    String startTime;
    String endTime;
    String specificDate;
    Boolean isRecurring;
    UnavailabilityStatus status;
}
