package com.futurenbetter.saas.modules.inventory.dto.response;

import com.futurenbetter.saas.modules.inventory.enums.Status;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IngredientBatchResponse {
    Long id;
    String ingredientName;
    String batchCode;
    LocalDate expiredAt;
    LocalDate receivedDate;

    Integer initialQuantity;
    Integer currentQuantity;
    Double importPrice;
    Status status;
}
