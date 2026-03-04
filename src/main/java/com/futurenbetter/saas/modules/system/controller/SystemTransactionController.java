package com.futurenbetter.saas.modules.system.controller;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.product.dto.response.CategoryResponse;
import com.futurenbetter.saas.modules.system.dto.filter.SystemTransactionFilter;
import com.futurenbetter.saas.modules.system.dto.request.SystemTransactionRequest;
import com.futurenbetter.saas.modules.system.dto.response.SystemTransactionResponse;
import com.futurenbetter.saas.modules.system.service.inter.SystemTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/transactions")
@RequiredArgsConstructor
public class SystemTransactionController {

    private final SystemTransactionService systemTransactionService;


    @PostMapping
    @PreAuthorize("hasAuthority('system-transaction:create')")
    public ApiResponse<SystemTransactionResponse> create(
            @RequestBody SystemTransactionRequest request
    ) {

        SystemTransactionResponse response = systemTransactionService.create(request);

        return ApiResponse.success(
                HttpStatus.CREATED,
                "Create system transaction successfully",
                response,
                null
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system-transaction:read')")
    public ApiResponse<List<SystemTransactionResponse>> getAll(
            @ModelAttribute SystemTransactionFilter filter
    ) {
        Page<SystemTransactionResponse> page = systemTransactionService.findAll(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(page.getNumber() + 1)
                .size(page.getSize())
                .lastPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Get system transactions successfully",
                page.getContent(),
                meta);
    }
}
