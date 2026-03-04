package com.futurenbetter.saas.modules.system.dto.filter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.modules.system.enums.SystemTransactionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SystemTransactionFilter extends BaseFilter {
    Long approverId;
    Boolean isIncome;
    LocalDateTime fromDate;
    LocalDateTime toDate;
    SystemTransactionType type;
}
