package com.futurenbetter.saas.modules.auth.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.auth.dto.filter.CustomerFilter;
import com.futurenbetter.saas.modules.auth.dto.request.UpdateProfileRequest;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.dto.response.MembershipRankResponse;
import com.futurenbetter.saas.modules.auth.entity.Customer;
import com.futurenbetter.saas.modules.auth.mapper.CustomerMapper;
import com.futurenbetter.saas.modules.auth.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('customer:me')")
    public ResponseEntity<CustomerResponse> getMyProfile(@AuthenticationPrincipal Customer customer) {
        CustomerResponse response = customerService.getCustomer(customer);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('customer:me')")
    public ResponseEntity<CustomerResponse> updateMyProfile(
            @AuthenticationPrincipal Customer customer,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(customerService.updateProfile(customer.getId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:update')")
    public ApiResponse<CustomerResponse> updateCustomerProfile(
            @PathVariable Long id,
            @RequestBody UpdateProfileRequest request
    ) {

        CustomerResponse response = customerService.updateProfile(id, request);

        return ApiResponse.success(
                HttpStatus.OK,
                "Cập nhật thông tin khách hàng thành công",
                response,
                null);
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('customer:read')")
    public ApiResponse<List<CustomerResponse>> getAll(
            CustomerFilter filter
    ) {
        Page<CustomerResponse> responses = customerService.getCustomers(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(responses.getNumber() + 1)
                .size(responses.getSize())
                .lastPage(responses.getTotalPages())
                .totalElements(responses.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy danh sách khách hàng thành công",
                responses.getContent(),
                meta);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:read-detail')")
    public ApiResponse<CustomerResponse> getCustomerById(
            @PathVariable Long id
    ) {
        CustomerResponse response = customerService.getCustomerById(id);

        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy chi tiết khách hàng thành công",
                response,
                null);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:delete')")
    public ApiResponse<CustomerResponse> deleteCustomer(
            @PathVariable Long id
    ) {
        CustomerResponse response = customerService.deleteCustomer(id);

        return ApiResponse.success(
                HttpStatus.OK,
                "Xóa khách hàng thành công",
                response,
                null);
    }
}
