package com.futurenbetter.saas.modules.inventory.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.modules.inventory.dto.request.RecipeRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.RecipeResponse;
import com.futurenbetter.saas.modules.inventory.service.inter.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/inventory/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping
    public ApiResponse<RecipeResponse> create(
            @RequestBody @Valid RecipeRequest request
    ) {
        RecipeResponse response = recipeService.create(request);

        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create recipe successfully",
                response,
                null
        );
    }

    @PutMapping("{id}")
    public ApiResponse<RecipeResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid RecipeRequest request
    ) {
        RecipeResponse response = recipeService.update(id, request);

        return ApiResponse.success(
                HttpStatus.OK,
                "Update recipe successfully",
                response,
                null
        );
    }

    @GetMapping("variant/{variantId}")
    public ApiResponse<List<RecipeResponse>> getByVariant(
            @PathVariable Long variantId
    ) {
        List<RecipeResponse> response = recipeService.getByVariant(variantId);
        return ApiResponse.success(HttpStatus.OK, "Get recipes by variant successfully", response, null);
    }

    @GetMapping("topping/{toppingId}")
    public ApiResponse<List<RecipeResponse>> getByTopping(
            @PathVariable Long toppingId
    ) {
        List<RecipeResponse> response = recipeService.getByTopping(toppingId);
        return ApiResponse.success(HttpStatus.OK, "Get recipes by topping successfully", response, null);
    }
}
