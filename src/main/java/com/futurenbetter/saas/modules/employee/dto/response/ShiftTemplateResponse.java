package com.futurenbetter.saas.modules.employee.dto.response;

import com.futurenbetter.saas.modules.employee.enums.ShiftTemplateStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShiftTemplateResponse {
    Long shiftTemplateId;
    String name;
    String startTime;
    String endTime;
    ShiftTemplateStatus status;
    LocalDateTime updatedAt;
}
