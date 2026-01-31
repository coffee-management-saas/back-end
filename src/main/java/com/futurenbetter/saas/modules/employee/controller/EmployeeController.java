package com.futurenbetter.saas.modules.employee.controller;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.employee.dto.request.EmployeeRequest;
import com.futurenbetter.saas.modules.employee.dto.response.EmployeeResponse;
import com.futurenbetter.saas.modules.employee.service.inter.EmployeeService;
import com.futurenbetter.saas.modules.product.dto.request.CategoryRequest;
import com.futurenbetter.saas.modules.product.dto.response.CategoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/employee/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ApiResponse<EmployeeResponse> create(
            @RequestBody @Valid EmployeeRequest request
    ) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create employee successfully",
                response,
                null
        );
    }

    @PutMapping("{id}")
    public ApiResponse<EmployeeResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid EmployeeRequest request
    ) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ApiResponse.success(
                HttpStatus.OK,
                "Update employee successfully",
                response,
                null
        );
    }

    @GetMapping("{id}")
    public ApiResponse<EmployeeResponse> getDetail(
            @PathVariable Long id
    ) {
        EmployeeResponse response = employeeService.getById(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Get employee detail successfully",
                response,
                null
        );
    }

    @GetMapping
    public ApiResponse<List<EmployeeResponse>> getAll(
            @ModelAttribute BaseFilter filter
    ) {
        Page<EmployeeResponse> page = employeeService.getAll(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(page.getNumber() + 1)
                .size(page.getSize())
                .lastPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Get employees successfully",
                page.getContent(),
                meta
        );
    }

    @DeleteMapping("{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id
    ) {
        employeeService.deleteEmployee(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Delete employee successfully",
                null,
                null
        );
    }

}
