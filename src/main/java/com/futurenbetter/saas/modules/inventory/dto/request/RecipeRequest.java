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
public class RecipeRequest {

    @NotNull(message = "Nguyên liệu ID là bắt buộc")
    Long ingredientId;

    Long variantId;
    Long toppingId;

    @NotNull(message = "Định lượng là bắt buộc")
    @Min(value = 1, message = "Định lượng phải lớn hơn 0")
    Double quantityRequired;

    String note;
}
