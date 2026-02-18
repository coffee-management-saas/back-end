package com.futurenbetter.saas.modules.product.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.modules.product.dto.request.SizeRequest;
import com.futurenbetter.saas.modules.product.dto.response.SizeResponse;
import com.futurenbetter.saas.modules.product.entity.Size;
import com.futurenbetter.saas.modules.product.enums.SizeStatus;
import com.futurenbetter.saas.modules.product.service.inter.SizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/product/sizes")
@RequiredArgsConstructor
public class SizeController {

    private final SizeService sizeService;

    @PostMapping
    @Transactional
    @PreAuthorize("hasAuthority('product-size:create')")
    public ResponseEntity<SizeResponse> create(
            @RequestBody @Valid SizeRequest request
    ) {
        SizeResponse response = sizeService.create(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("{id}")
    @Transactional
    @PreAuthorize("hasAuthority('product-size:update')")
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
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('product-size:read')")
    public List<SizeResponse> getAll(
            @RequestParam(required = false) SizeStatus status
    ) {
        return sizeService.getAll(status);
    }

    @GetMapping("active")
    @PreAuthorize("hasAuthority('product-size:read-active')")
    public ApiResponse<List<Size>> getActive() {
        return ApiResponse.success(
                HttpStatus.OK,
                "Get active sizes",
                sizeService.getActiveSizes(),
                null
        );
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('product-size:delete')")
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