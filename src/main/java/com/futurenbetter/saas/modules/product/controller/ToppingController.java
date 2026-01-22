package com.futurenbetter.saas.modules.product.controller;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.product.dto.request.ToppingRequest;
import com.futurenbetter.saas.modules.product.dto.response.ToppingResponse;
import com.futurenbetter.saas.modules.product.service.inter.ToppingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/product/toppings")
@RequiredArgsConstructor
public class ToppingController {

    private final ToppingService toppingService;

    @PostMapping
    public ApiResponse<ToppingResponse> create(
            @RequestBody @Valid ToppingRequest request
    ) {
        ToppingResponse response = toppingService.create(request);
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create topping successfully",
                response,
                null
        );
    }

    @PutMapping("{id}")
    public ApiResponse<ToppingResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid ToppingRequest request
    ) {
        ToppingResponse response = toppingService.update(id, request);
        return ApiResponse.success(
                HttpStatus.OK,
                "Update topping successfully",
                response,
                null
        );
    }

    @GetMapping("{id}")
    public ApiResponse<ToppingResponse> getDetail(
            @PathVariable Long id
    ) {
        ToppingResponse response = toppingService.getDetail(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Get topping detail successfully",
                response,
                null
        );
    }

    @GetMapping
    public ApiResponse<List<ToppingResponse>> getAll(
            @ModelAttribute BaseFilter filter
    ) {
        Page<ToppingResponse> page = toppingService.getAll(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(page.getNumber() + 1)
                .size(page.getSize())
                .lastPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Get toppings successfully",
                page.getContent(),
                meta
        );
    }

    @DeleteMapping("{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id
    ) {
        toppingService.delete(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Delete topping successfully",
                null,
                null
        );
    }
}