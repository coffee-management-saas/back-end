package com.futurenbetter.saas.modules.auth.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.auth.dto.filter.ShopFilter;
import com.futurenbetter.saas.modules.auth.dto.request.ShopRequest;
import com.futurenbetter.saas.modules.auth.dto.response.ShopResponse;
import com.futurenbetter.saas.modules.auth.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @GetMapping
    @PreAuthorize("hasAuthority('shop:read')")
    public ApiResponse<List<ShopResponse>> getShops(
            ShopFilter filter
    ) {
        Page<ShopResponse> responses = shopService.getShops(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(responses.getNumber() + 1)
                .size(responses.getSize())
                .lastPage(responses.getTotalPages())
                .totalElements(responses.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy danh sách cửa hàng thành công",
                responses.getContent(),
                meta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('shop:read-detail')")
    public ApiResponse<ShopResponse> getShopById(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy thông tin cửa hàng thành công",
                shopService.getShopById(id),
                null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('shop:update')")
    public ApiResponse<ShopResponse> updateShop(
            @PathVariable Long id,
            @RequestBody ShopRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.OK,
                "Cập nhật cửa hàng thành công",
                shopService.updateShop(id, request),
                null);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('shop:create')")
    public ApiResponse<ShopResponse> createShop(
            @RequestBody ShopRequest request
    ) {
        ShopResponse response = shopService.createShop(request);

        return ApiResponse.success(
                HttpStatus.OK,
                "Tạo cửa hàng thành công",
                response,
                null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('shop:delete')")
    public ApiResponse<ShopResponse> deleteShop(
            @PathVariable Long id
    ) {
        ShopResponse response = shopService.deleteShop(id);

        return ApiResponse.success(
                HttpStatus.OK,
                "Xóa cửa hàng thành công",
                response,
                null);
    }
}
