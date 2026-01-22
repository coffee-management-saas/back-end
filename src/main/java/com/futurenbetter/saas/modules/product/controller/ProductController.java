package com.futurenbetter.saas.modules.product.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.product.dto.filter.ProductFilter;
import com.futurenbetter.saas.modules.product.dto.request.ProductRequest;
import com.futurenbetter.saas.modules.product.dto.response.ProductResponse;
import com.futurenbetter.saas.modules.product.service.inter.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/product/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponse> create(
            @RequestBody @Valid ProductRequest request
    ) {
        ProductResponse response = productService.create(request);
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create product successfully",
                response,
                null
        );
    }

    @PutMapping("{id}")
    public ApiResponse<ProductResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequest request
    ) {
        ProductResponse response = productService.update(id, request);
        return ApiResponse.success(
                HttpStatus.OK,
                "Update product successfully",
                response,
                null
        );
    }

    @GetMapping("{id}")
    public ApiResponse<ProductResponse> getDetail(
            @PathVariable Long id
    ) {
        ProductResponse response = productService.getDetail(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Get product detail successfully",
                response,
                null
        );
    }

    @GetMapping
    public ApiResponse<List<ProductResponse>> getAll(
            @ModelAttribute ProductFilter filter
    ) {
        Page<ProductResponse> page = productService.getAll(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(page.getNumber() + 1)
                .size(page.getSize())
                .lastPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Get products successfully",
                page.getContent(),
                meta
        );
    }

    // --- API cấu hình Topping cho Product ---

    @PostMapping("{id}/allow-toppings")
    public ApiResponse<Void> updateAllowToppings(
            @PathVariable Long id,
            @RequestBody List<Long> toppingIds
    ) {
        productService.updateAllowToppings(id, toppingIds);
        return ApiResponse.success(
                HttpStatus.OK,
                "Update allowed toppings successfully",
                null,
                null
        );
    }

    @GetMapping("{id}/allow-toppings")
    public ApiResponse<List<Long>> getAllowToppings(
            @PathVariable Long id
    ) {
        List<Long> toppingIds = productService.getAllowToppingIds(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Get allowed toppings successfully",
                toppingIds,
                null
        );
    }
}