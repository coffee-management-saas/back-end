package com.futurenbetter.saas.modules.inventory.service.inter;

import com.futurenbetter.saas.modules.inventory.dto.request.RecipeRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.RecipeResponse;

import java.util.List;

public interface RecipeService {
    RecipeResponse create(RecipeRequest request);
    RecipeResponse update(Long id, RecipeRequest request);
    List<RecipeResponse> getByVariant(Long variantId);
    List<RecipeResponse> getByTopping(Long toppingId);
}
