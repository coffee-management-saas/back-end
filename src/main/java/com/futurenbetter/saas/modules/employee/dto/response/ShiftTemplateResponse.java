package com.futurenbetter.saas.modules.employee.dto.response;

import com.futurenbetter.saas.modules.employee.enums.ShiftTemplateStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShiftTemplateResponse {
    Long shiftTemplateId;
    String name;
    LocalTime startTime;
    LocalTime endTime;
    ShiftTemplateStatus status;
    LocalDateTime updatedAt;
}
