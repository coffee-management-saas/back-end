package com.futurenbetter.saas.modules.inventory.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecipeResponse {
    Long id;
    Long ingredientId;
    String ingredientName;
    String unitName;
    Long variantId;
    Long toppingId;
    Integer quantityRequired;
    String note;
}
