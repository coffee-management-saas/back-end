package com.futurenbetter.saas.modules.employee.controller;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.employee.dto.request.ShiftTemplateRequest;
import com.futurenbetter.saas.modules.employee.dto.response.ShiftTemplateResponse;
import com.futurenbetter.saas.modules.employee.service.inter.ShiftTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/employee/shift-templates")
@RequiredArgsConstructor
public class ShiftTemplateController {

    private final ShiftTemplateService shiftTemplateService;

    @PostMapping
    public ApiResponse<ShiftTemplateResponse> create(
            @RequestBody @Valid ShiftTemplateRequest request
    ) {
        ShiftTemplateResponse response = shiftTemplateService.create(request);
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create shift template successfully",
                response,
                null
        );
    }

    @PutMapping("{id}")
    public ApiResponse<ShiftTemplateResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid ShiftTemplateRequest request
    ) {
        ShiftTemplateResponse response = shiftTemplateService.update(id, request);
        return ApiResponse.success(
                HttpStatus.OK,
                "Update shift template successfully",
                response,
                null
        );
    }

    @GetMapping("{id}")
    public ApiResponse<ShiftTemplateResponse> getDetail(
            @PathVariable Long id
    ) {
        ShiftTemplateResponse response = shiftTemplateService.getDetail(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Get shift template detail successfully",
                response,
                null
        );
    }

    @GetMapping
    public ApiResponse<List<ShiftTemplateResponse>> getAll(
            @ModelAttribute BaseFilter filter
    ) {
        Page<ShiftTemplateResponse> page = shiftTemplateService.getAll(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(page.getNumber() + 1)
                .size(page.getSize())
                .lastPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Get shift templates successfully",
                page.getContent(),
                meta
        );
    }

    @DeleteMapping("{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id
    ) {
        shiftTemplateService.delete(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Delete shift template successfully",
                null,
                null
        );
    }
}