package com.futurenbetter.saas.modules.employee.controller;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.employee.dto.request.ScheduleRequest;
import com.futurenbetter.saas.modules.employee.dto.response.ScheduleResponse;
import com.futurenbetter.saas.modules.employee.service.inter.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/employee/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ApiResponse<ScheduleResponse> create(
            @RequestBody @Valid ScheduleRequest request
    ) {
        ScheduleResponse response = scheduleService.create(request);
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create schedule successfully",
                response,
                null
        );
    }

    @PutMapping("{id}")
    public ApiResponse<ScheduleResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid ScheduleRequest request
    ) {
        ScheduleResponse response = scheduleService.update(id, request);
        return ApiResponse.success(
                HttpStatus.OK,
                "Update schedule successfully",
                response,
                null
        );
    }

    @GetMapping("{id}")
    public ApiResponse<ScheduleResponse> getDetail(
            @PathVariable Long id
    ) {
        ScheduleResponse response = scheduleService.getDetail(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Get schedule detail successfully",
                response,
                null
        );
    }

    @GetMapping
    public ApiResponse<List<ScheduleResponse>> getAll(
            @ModelAttribute BaseFilter filter
    ) {
        Page<ScheduleResponse> page = scheduleService.getAll(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(page.getNumber() + 1)
                .size(page.getSize())
                .lastPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Get schedules successfully",
                page.getContent(),
                meta
        );
    }

    @GetMapping("{employeeId}")
    public ApiResponse<List<ScheduleResponse>> getByEmployee(
            @PathVariable Long employeeId
    ) {
        List<ScheduleResponse> list = scheduleService.getByEmployeeId(employeeId);
        return ApiResponse.success(
                HttpStatus.OK,
                "Get schedules by employee successfully",
                list,
                null
        );
    }

    @DeleteMapping("{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id
    ) {
        scheduleService.delete(id);
        return ApiResponse.success(
                HttpStatus.OK,
                "Delete schedule successfully",
                null,
                null
        );
    }
}