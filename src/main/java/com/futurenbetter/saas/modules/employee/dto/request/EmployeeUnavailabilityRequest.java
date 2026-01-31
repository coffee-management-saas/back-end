package com.futurenbetter.saas.modules.employee.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeUnavailabilityRequest {
    Long employeeId;
    LocalDateTime startTime;
    LocalDateTime endTime;
    LocalDateTime specificDate;
    String reason;
    Boolean isRecurring;
}
