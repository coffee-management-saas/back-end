package com.futurenbetter.saas.modules.auth.controller;

import com.futurenbetter.saas.modules.auth.dto.request.UpdateProfileRequest;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.entity.Customer;
import com.futurenbetter.saas.modules.auth.mapper.CustomerMapper;
import com.futurenbetter.saas.modules.auth.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerMapper customerMapper;
    private final CustomerService customerService;

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile(@AuthenticationPrincipal Customer customer) {
        CustomerResponse response = customerService.getCustomer(customer);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateMyProfile(
            @AuthenticationPrincipal Customer customer,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(customerService.updateProfile(customer.getId(), request));
    }
}
