package com.futurenbetter.saas.modules.system.dto.request;

import com.futurenbetter.saas.modules.system.enums.SystemTransactionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SystemTransactionRequest {
    long amount;
    boolean isIncome;
    SystemTransactionType type;
}
