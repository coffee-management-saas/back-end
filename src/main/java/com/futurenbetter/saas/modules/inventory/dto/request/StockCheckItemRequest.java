package com.futurenbetter.saas.modules.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockCheckItemRequest {
    @NotNull
    Long ingredientId;

    @NotNull
    @Min(0)
    Double actualQuantity; // Số thực tế đếm được
    String reason;
}
