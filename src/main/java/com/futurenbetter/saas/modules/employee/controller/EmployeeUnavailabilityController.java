package com.futurenbetter.saas.modules.employee.controller;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.employee.dto.request.EmployeeUnavailabilityRequest;
import com.futurenbetter.saas.modules.employee.dto.response.EmployeeUnavailabilityResponse;
import com.futurenbetter.saas.modules.employee.service.inter.EmployeeUnavailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/employee/unavailabilities")
@RequiredArgsConstructor
public class EmployeeUnavailabilityController {

    private final EmployeeUnavailabilityService unavailabilityService;

    @PostMapping
    public ApiResponse<EmployeeUnavailabilityResponse> create(
            @RequestBody @Valid EmployeeUnavailabilityRequest request
    ) {
        EmployeeUnavailabilityResponse response = unavailabilityService.create(request);
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create unavailability record successfully",
                response,
                null
        );
    }

    @PutMapping("{id}")
    public ApiResponse<EmployeeUnavailabilityResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid EmployeeUnavailabilityRequest request
    ) {
        EmployeeUnavailabilityResponse response = unavailabilityService.update(id, request);
        return ApiResponse.success(
                HttpStatus.OK,
                "Update unavailability record successfully",
                response,
                null
        );
    }

    @GetMapping("{id}")
    public ApiResponse<EmployeeUnavailabilityResponse> getDetail(
            @PathVariable Long id
    ) {
        EmployeeUnavailabilityResponse response = unavailabilityService.getDetail(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Get unavailability detail successfully",
                response,
                null
        );
    }

    @GetMapping
    public ApiResponse<List<EmployeeUnavailabilityResponse>> getAll(
            @ModelAttribute BaseFilter filter
    ) {
        Page<EmployeeUnavailabilityResponse> page = unavailabilityService.getAll(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(page.getNumber() + 1)
                .size(page.getSize())
                .lastPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Get unavailability list successfully",
                page.getContent(),
                meta
        );
    }

    @DeleteMapping("{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id
    ) {
        unavailabilityService.delete(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Delete unavailability record successfully",
                null,
                null
        );
    }
}