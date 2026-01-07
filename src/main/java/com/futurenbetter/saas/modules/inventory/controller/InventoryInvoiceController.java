package com.futurenbetter.saas.modules.inventory.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.inventory.dto.filter.InventoryInvoiceFilter;
import com.futurenbetter.saas.modules.inventory.dto.request.InventoryInvoiceRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.InventoryInvoiceResponse;
import com.futurenbetter.saas.modules.inventory.service.inter.InventoryInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/inventory/invoices")
@RequiredArgsConstructor
public class InventoryInvoiceController {

    private final InventoryInvoiceService invoiceService;

    @PostMapping
    public ApiResponse<InventoryInvoiceResponse> importStock(
            @RequestBody @Valid InventoryInvoiceRequest request
    ) {
        InventoryInvoiceResponse response = invoiceService.importStock(request);

        return ApiResponse.success(
                HttpStatus.CREATED,
                "Import stock successfully",
                response,
                null
        );
    }

    @GetMapping
    public ApiResponse<List<InventoryInvoiceResponse>> getByFilter(
            @ModelAttribute InventoryInvoiceFilter filter
    ) {
        Page<InventoryInvoiceResponse> responses = invoiceService.getAll(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(responses.getNumber() + 1)
                .size(responses.getSize())
                .lastPage(responses.getTotalPages())
                .totalElements(responses.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Get invoices successfully",
                responses.getContent(),
                meta
        );
    }

    @GetMapping("{id}")
    public ApiResponse<InventoryInvoiceResponse> getDetail(
            @PathVariable Long id
    ) {
        InventoryInvoiceResponse response = invoiceService.getDetail(id);

        return ApiResponse.success(
                HttpStatus.OK,
                "Get invoice detail successfully",
                response,
                null
        );
    }
}
