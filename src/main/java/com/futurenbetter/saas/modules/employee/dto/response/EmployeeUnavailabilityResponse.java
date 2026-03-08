package com.futurenbetter.saas.modules.employee.dto.response;

import com.futurenbetter.saas.modules.employee.enums.UnavailabilityStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeUnavailabilityResponse {
    Long employeeUnavailabilityId;
    Long employeeId;
    String employeeName;
    String reason;
    LocalDateTime startTime;
    LocalDateTime endTime;
    LocalDateTime specificDate;
    Boolean isRecurring;
    UnavailabilityStatus status;
}
