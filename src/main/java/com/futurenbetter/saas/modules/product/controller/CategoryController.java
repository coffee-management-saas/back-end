package com.futurenbetter.saas.modules.product.controller;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.product.dto.request.CategoryRequest;
import com.futurenbetter.saas.modules.product.dto.response.CategoryResponse;
import com.futurenbetter.saas.modules.product.service.inter.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

        private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('category:create')")
    public ApiResponse<CategoryResponse> create(
            @RequestBody @Valid CategoryRequest request
    ) {
        CategoryResponse response = categoryService.create(request);
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create category successfully",
                response,
                null
        );
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('category:update')")
    public ApiResponse<CategoryResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid CategoryRequest request
    ) {
        CategoryResponse response = categoryService.update(id, request);
        return ApiResponse.success(
                HttpStatus.OK,
                "Update category successfully",
                response,
                null
        );
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('category:read-detail')")
    public ApiResponse<CategoryResponse> getDetail(
            @PathVariable Long id
    ) {
        CategoryResponse response = categoryService.getDetail(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Get category detail successfully",
                response,
                null
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('category:read')")
    public ApiResponse<List<CategoryResponse>> getAll(
            @ModelAttribute BaseFilter filter
    ) {
        Page<CategoryResponse> page = categoryService.getAll(filter);

                PageMeta meta = PageMeta.builder()
                                .currentPage(page.getNumber() + 1)
                                .size(page.getSize())
                                .lastPage(page.getTotalPages())
                                .totalElements(page.getTotalElements())
                                .build();

                return ApiResponse.success(
                                HttpStatus.OK,
                                "Get categories successfully",
                                page.getContent(),
                                meta);
        }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('category:delete')")
    public ApiResponse<Void> delete(
            @PathVariable Long id
    ) {
        categoryService.delete(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Delete category successfully",
                null,
                null
        );
    }
}