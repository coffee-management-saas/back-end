package com.futurenbetter.saas.modules.order.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.order.dto.filter.OrderFilter;
import com.futurenbetter.saas.modules.order.dto.request.OrderRequest;
import com.futurenbetter.saas.modules.order.dto.response.OrderResponse;
import com.futurenbetter.saas.modules.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        OrderResponse response = orderService.createOrder(orderRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('order:read-history')")
    public ApiResponse<List<OrderResponse>> getOrderHistory(OrderFilter filter) {
        Page<OrderResponse> page = orderService.getOrderHistory(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(page.getNumber() + 1)
                .size(page.getSize())
                .lastPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy lịch sử đơn hàng thành công",
                page.getContent(),
                meta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/initiate-payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> initiatePayment(
            @PathVariable Long id,
            @RequestParam String returnUrl) {
        OrderResponse response = orderService.initiatePayment(id, returnUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirm-cash")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> confirmCashPayment(@PathVariable Long id) {
        OrderResponse response = orderService.confirmCashPayment(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/v2")
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<CreatePaymentLinkResponse> createOrderv2(@Valid @RequestBody OrderRequest orderRequest) {
        CreatePaymentLinkResponse response = orderService.createOrderv2(orderRequest);
        return ResponseEntity.ok(response);
    }
}
