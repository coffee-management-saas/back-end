package com.futurenbetter.saas.modules.auth.controller;

import com.futurenbetter.saas.modules.auth.dto.request.CustomerRegistrationRequest;
import com.futurenbetter.saas.modules.auth.dto.request.LoginRequest;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.dto.response.LoginResponse;
import com.futurenbetter.saas.modules.auth.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final CustomerService customerService;

    @PostMapping("/register")   
    public ResponseEntity<CustomerResponse> register(@RequestBody CustomerRegistrationRequest request) {
        // Backend tự biết shopId là bao nhiêu, khách không cần truyền lên
        return ResponseEntity.ok(customerService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(customerService.login(request));
    }
}
