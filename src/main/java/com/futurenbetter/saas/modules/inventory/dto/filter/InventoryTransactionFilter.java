package com.futurenbetter.saas.modules.inventory.dto.filter;

import com.futurenbetter.saas.modules.inventory.enums.Status;
import com.futurenbetter.saas.modules.inventory.enums.TransactionType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransactionFilter {
    Long id;
    Long shopId;
    Long ingredientId;
    TransactionType transactionType;
    Long ingredientBatchId;
    Long inventoryInvoiceId;
    Long stockCheckDetailId;
    Long orderId;
    Integer minQuantityChange;
    Integer maxQuantityChange;
    Integer minQuantityAfter;
    Integer maxQuantityAfter;
    LocalDateTime fromDate; // created after this date
    LocalDateTime toDate; // updated before this date
    Status status;
}
