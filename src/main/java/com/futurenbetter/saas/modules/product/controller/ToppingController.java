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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/toppings")
@RequiredArgsConstructor
public class ToppingController {

    private final ToppingService toppingService;

    @PostMapping
    @PreAuthorize("hasAuthority('topping:create')")
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
    @PreAuthorize("hasAuthority('topping:update')")
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
//    @PreAuthorize("hasAuthority('topping:read-detail')")
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
//    @PreAuthorize("hasAuthority('topping:read')")
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
    @PreAuthorize("hasAuthority('topping:delete')")
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