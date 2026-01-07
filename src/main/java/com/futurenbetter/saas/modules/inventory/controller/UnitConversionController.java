package com.futurenbetter.saas.modules.inventory.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.modules.inventory.dto.request.UnitConversionRequest;
import com.futurenbetter.saas.modules.inventory.entity.UnitConversion;
import com.futurenbetter.saas.modules.inventory.service.inter.UnitConversionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/inventory/unit-conversions")
@RequiredArgsConstructor
public class UnitConversionController {

    private final UnitConversionService unitConversionService;

    @PostMapping
    public ApiResponse<UnitConversion> create(
            @RequestBody @Valid UnitConversionRequest request
    ) {
        UnitConversion response = unitConversionService.create(request);

        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create unit conversion successfully",
                response,
                null
        );
    }

    @PutMapping("{id}")
    public ApiResponse<UnitConversion> update(
            @PathVariable Long id,
            @RequestBody @Valid UnitConversionRequest request
    ) {
        UnitConversion response = unitConversionService.update(id, request);

        return ApiResponse.success(
                HttpStatus.OK,
                "Update unit conversion successfully",
                response,
                null
        );
    }
}
