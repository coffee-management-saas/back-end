package com.futurenbetter.saas.modules.inventory.dto.response;

import com.futurenbetter.saas.modules.inventory.enums.InputUnit;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceItemResponse {
    Long ingredientId;
    String ingredientName;
    InputUnit inputUnit;
    Integer inputQuantity;
    Double unitPrice;
    Integer convertedQuantity;
    String baseUnit;
    String batchCode;
    LocalDate expiredAt;
}
