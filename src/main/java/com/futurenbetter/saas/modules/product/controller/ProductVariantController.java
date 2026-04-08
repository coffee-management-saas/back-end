package com.futurenbetter.saas.modules.product.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.product.dto.filter.ProductVariantFilter;
import com.futurenbetter.saas.modules.product.dto.request.ProductVariantRequest;
import com.futurenbetter.saas.modules.product.dto.response.ProductVariantResponse;
import com.futurenbetter.saas.modules.product.service.inter.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService variantService;

    @PostMapping
    @PreAuthorize("hasAuthority('product-variant:create')")
    public ApiResponse<ProductVariantResponse> create(
            @RequestBody @Valid ProductVariantRequest request
    ) {
        ProductVariantResponse response = variantService.create(request);
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create product variant successfully",
                response,
                null
        );
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('product-variant:update')")
    public ApiResponse<ProductVariantResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid ProductVariantRequest request
    ) {
        ProductVariantResponse response = variantService.update(id, request);
        return ApiResponse.success(
                HttpStatus.OK,
                "Update product variant successfully",
                response,
                null
        );
    }

    @GetMapping("{id}")
//    @PreAuthorize("hasAuthority('product-variant:read-detail')")
    public ApiResponse<ProductVariantResponse> getDetail(
            @PathVariable Long id
    ) {
        ProductVariantResponse response = variantService.getDetail(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Get variant detail successfully",
                response,
                null
        );
    }

    @GetMapping("by-product/{productId}")
//    @PreAuthorize("hasAuthority('product-variant:read-by-product')")
    public ApiResponse<List<ProductVariantResponse>> getByProduct(
            @PathVariable Long productId
    ) {
        List<ProductVariantResponse> list = variantService.getByProductId(productId);
        return ApiResponse.success(
                HttpStatus.OK,
                "Get variants by product successfully",
                list,
                null
        );
    }

    @GetMapping
//    @PreAuthorize("hasAuthority('product-variant:read')")
    public ApiResponse<List<ProductVariantResponse>> getAll(
            @ModelAttribute ProductVariantFilter filter
    ) {
        Page<ProductVariantResponse> page = variantService.getAll(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(page.getNumber() + 1)
                .size(page.getSize())
                .lastPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Get variants successfully",
                page.getContent(),
                meta
        );
    }
}