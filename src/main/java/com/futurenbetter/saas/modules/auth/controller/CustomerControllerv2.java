package com.futurenbetter.saas.modules.auth.controller;

import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.entity.Customer;
import com.futurenbetter.saas.modules.auth.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class CustomerControllerv2 {
    private final CustomerService customerService;

    @GetMapping("")
    @PreAuthorize("hasAuthority('customer:me')")
    public ResponseEntity<CustomerResponse> getMyProfile(@AuthenticationPrincipal Customer customer) {
        CustomerResponse response = customerService.getCustomer(customer);
        return ResponseEntity.ok(response);
    }
}
