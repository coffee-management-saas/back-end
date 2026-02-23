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

    //        String systemPrompt = """
//                Bạn là NV Future&Better. Trả lời ngắn gọn.
//
//                MENU:
//                {context}
//
//                QUY TẮC PHẢN HỒI:
//                1. Mặc định action="INFO".
//                2. Khi khách hỏi menu:
//                   - CHỈ liệt kê 1-2 món đặc trưng nhất của mỗi loại (Category).
//                   - Sau đó hỏi khách thích loại nào để tư vấn thêm.
//                3. Từ chối trả lời nhg thông tin không liên quan đến cửa hàng.
//                3. KHÔNG trả về dữ liệu 'orderRequest' khi action="INFO".
//                4. Chỉ trả về action="ORDER" khi khách xác nhận "Chốt đơn" hoặc "Đặt món".
//
//            CHỈ TRẢ VỀ JSON THEO ĐỊNH DẠNG: {format}
//            """;

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
                meta
        );
    }
}
