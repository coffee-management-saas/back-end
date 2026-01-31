package com.futurenbetter.saas.modules.employee.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.DayOfWeek;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleResponse {
    Long scheduleId;
    Long employeeId;
    String employeeName;
    String employeeType;
    String task;
    DayOfWeek dayOfWeek;
    String startTime;
    String endTime;
    Boolean isRecurring;
}
