package com.futurenbetter.saas.modules.product.controller;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.product.dto.request.SizeRequest;
import com.futurenbetter.saas.modules.product.entity.Size;
import com.futurenbetter.saas.modules.product.service.inter.SizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/product/sizes")
@RequiredArgsConstructor
public class SizeController {

    private final SizeService sizeService;

    @PostMapping
    public ApiResponse<Size> create(
            @RequestBody @Valid SizeRequest request
    ) {
        Size response = sizeService.create(request);
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create size successfully",
                response,
                null
        );
    }

    @PutMapping("{id}")
    public ApiResponse<Size> update(
            @PathVariable Long id,
            @RequestBody @Valid SizeRequest request
    ) {
        Size response = sizeService.update(id, request);
        return ApiResponse.success(
                HttpStatus.OK,
                "Update size successfully",
                response,
                null
        );
    }

    @GetMapping
    public ApiResponse<List<Size>> getAll(
            @ModelAttribute BaseFilter filter
    ) {
        Page<Size> page = sizeService.getAll(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(page.getNumber() + 1)
                .size(page.getSize())
                .lastPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Get sizes successfully",
                page.getContent(),
                meta
        );
    }

    @GetMapping("active")
    public ApiResponse<List<Size>> getActive() {
        return ApiResponse.success(
                HttpStatus.OK,
                "Get active sizes",
                sizeService.getActiveSizes(),
                null
        );
    }

    @DeleteMapping("{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id
    ) {
        sizeService.delete(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Delete size successfully",
                null,
                null
        );
    }
}