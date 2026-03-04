package com.futurenbetter.saas.modules.system.dto.response;

import com.futurenbetter.saas.modules.system.enums.SystemTransactionType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SystemTransactionResponse {
    Long id;
    Long amount;
    Long approverId;
    Boolean isIncome;
    SystemTransactionType type;
    LocalDateTime createdAt;
}
