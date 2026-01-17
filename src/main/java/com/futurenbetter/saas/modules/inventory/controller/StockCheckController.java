package com.futurenbetter.saas.modules.inventory.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.inventory.dto.filter.StockCheckSessionFilter;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckApproveRequest;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckStartRequest;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckUpdateRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.StockCheckSessionResponse;
import com.futurenbetter.saas.modules.inventory.service.inter.StockCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/inventory/stock-checks")
@RequiredArgsConstructor
public class StockCheckController {

    private final StockCheckService stockCheckService;

    @PostMapping("start")
    public ApiResponse<StockCheckSessionResponse> startSession(
            @RequestBody @Valid StockCheckStartRequest request
    ) {
        StockCheckSessionResponse response = stockCheckService.startSession(request);

        return ApiResponse.success(
                HttpStatus.CREATED,
                "Start stock check session successfully",
                response,
                null
        );
    }

    @PutMapping("update-count")
    public ApiResponse<StockCheckSessionResponse> updateCount(
            @RequestBody @Valid StockCheckUpdateRequest request
    ) {
        StockCheckSessionResponse response = stockCheckService.updateCount(request);

        return ApiResponse.success(
                HttpStatus.OK,
                "Update stock counts successfully",
                response,
                null
        );
    }

    @PostMapping("approve")
    public ApiResponse<StockCheckSessionResponse> approveSession(
            @RequestBody @Valid StockCheckApproveRequest request
    ) {
        StockCheckSessionResponse response = stockCheckService.approveSession(request);

        return ApiResponse.success(
                HttpStatus.OK,
                "Approve stock check session successfully",
                response,
                null
        );
    }

    @GetMapping
    public ApiResponse<List<StockCheckSessionResponse>> getByFilter(
            @ModelAttribute StockCheckSessionFilter filter
    ) {
        Page<StockCheckSessionResponse> responses = stockCheckService.getAll(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(responses.getNumber() + 1)
                .size(responses.getSize())
                .lastPage(responses.getTotalPages())
                .totalElements(responses.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Get stock check sessions successfully",
                responses.getContent(),
                meta
        );
    }
}
