package com.futurenbetter.saas.modules.inventory.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.modules.inventory.dto.request.RecipeRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.RecipeResponse;
import com.futurenbetter.saas.modules.inventory.service.inter.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping
    @PreAuthorize("hasAuthority('recipe:create')")
    public ApiResponse<List<RecipeResponse>> create(
            @RequestBody @Valid RecipeRequest request) {
        List<RecipeResponse> response = recipeService.create(request);

        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create/Update recipe successfully",
                response,
                null);
    }

    @GetMapping("variant/{variantId}")
    @PreAuthorize("hasAuthority('recipe:read-by-variant')")
    public ApiResponse<List<RecipeResponse>> getByVariant(
            @PathVariable Long variantId) {
        List<RecipeResponse> response = recipeService.getByVariant(variantId);
        return ApiResponse.success(HttpStatus.OK, "Get recipes by variant successfully", response, null);
    }

    @GetMapping("topping/{toppingId}")
    @PreAuthorize("hasAuthority('recipe:read-by-topping')")
    public ApiResponse<List<RecipeResponse>> getByTopping(
            @PathVariable Long toppingId) {
        List<RecipeResponse> response = recipeService.getByTopping(toppingId);
        return ApiResponse.success(HttpStatus.OK, "Get recipes by topping successfully", response, null);
    }
}
