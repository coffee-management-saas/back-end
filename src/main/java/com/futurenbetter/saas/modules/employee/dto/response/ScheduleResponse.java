package com.futurenbetter.saas.modules.employee.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleResponse {
    Long scheduleId;
    Long employeeId;
    String employeeName;
    String employeeType;
    String task;
    String startTime;
    String endTime;
    Boolean isRecurring;
}
