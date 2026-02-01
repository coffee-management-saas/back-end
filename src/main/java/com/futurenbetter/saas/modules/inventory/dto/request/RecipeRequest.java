package com.futurenbetter.saas.modules.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecipeRequest {

    Long variantId;
    Long toppingId;

    @NotEmpty(message = "Công thức phải có ít nhất một nguyên liệu")
    @Valid
    List<RecipeItemRequest> items;
}
