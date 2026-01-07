package com.futurenbetter.saas.modules.inventory.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.inventory.dto.filter.RawIngredientFilter;
import com.futurenbetter.saas.modules.inventory.dto.request.RawIngredientRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.RawIngredientResponse;
import com.futurenbetter.saas.modules.inventory.service.inter.RawIngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/inventory/ingredients")
@RequiredArgsConstructor
public class RawIngredientController {

    private final RawIngredientService rawIngredientService;

    @PostMapping
    public ApiResponse<RawIngredientResponse> create(
            @RequestBody @Valid RawIngredientRequest request
    ) {
        RawIngredientResponse response = rawIngredientService.create(request);

        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create ingredient successfully",
                response,
                null
        );
    }

    @GetMapping
    public ApiResponse<List<RawIngredientResponse>> getByFilter(
            @ModelAttribute RawIngredientFilter filter
    ) {
        Page<RawIngredientResponse> responses = rawIngredientService.getAll(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(responses.getNumber() + 1)
                .size(responses.getSize())
                .lastPage(responses.getTotalPages())
                .totalElements(responses.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Get ingredients successfully",
                responses.getContent(),
                meta
        );
    }

    @GetMapping("{id}")
    public ApiResponse<RawIngredientResponse> getDetail(
            @PathVariable Long id
    ) {
        RawIngredientResponse response = rawIngredientService.getDetail(id);

        return ApiResponse.success(
                HttpStatus.OK,
                "Get ingredient detail successfully",
                response,
                null
        );
    }

    @PutMapping("{id}")
    public ApiResponse<RawIngredientResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid RawIngredientRequest request
    ) {
        RawIngredientResponse response = rawIngredientService.update(id, request);

        return ApiResponse.success(
                HttpStatus.OK,
                "Update ingredient successfully",
                response,
                null
        );
    }
}