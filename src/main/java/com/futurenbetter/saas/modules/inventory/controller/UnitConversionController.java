package com.futurenbetter.saas.modules.inventory.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.modules.inventory.dto.request.UnitConversionRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.UnitConversionResponse;
import com.futurenbetter.saas.modules.inventory.entity.UnitConversion;
import com.futurenbetter.saas.modules.inventory.service.inter.UnitConversionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory/unit-conversions")
@RequiredArgsConstructor
public class UnitConversionController {

    private final UnitConversionService unitConversionService;

    @PostMapping
    @PreAuthorize("hasAuthority('unit-conversion:create')")
    public ApiResponse<UnitConversionResponse> create(
            @RequestBody @Valid UnitConversionRequest request) {
        UnitConversionResponse response = unitConversionService.create(request);

        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create unit conversion successfully",
                response,
                null);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('unit-conversion:update')")
    public ApiResponse<UnitConversion> update(
            @PathVariable Long id,
            @RequestBody @Valid UnitConversionRequest request) {
        UnitConversion response = unitConversionService.update(id, request);

        return ApiResponse.success(
                HttpStatus.OK,
                "Update unit conversion successfully",
                response,
                null);
    }
}
