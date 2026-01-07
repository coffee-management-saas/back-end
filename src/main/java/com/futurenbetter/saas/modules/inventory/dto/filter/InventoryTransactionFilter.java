package com.futurenbetter.saas.modules.inventory.dto.filter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class InventoryTransactionFilter extends BaseFilter {
    Long ingredientId;
    Long batchId;
    String transactionType;
    String referenceCode;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime toDate;
}