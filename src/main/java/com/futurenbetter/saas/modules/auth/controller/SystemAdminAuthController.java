package com.futurenbetter.saas.modules.auth.controller;

import com.futurenbetter.saas.modules.auth.dto.request.SystemAdminLoginRequest;
import com.futurenbetter.saas.modules.auth.dto.request.SystemAdminRegistrationRequest;
import com.futurenbetter.saas.modules.auth.dto.response.SystemAdminLoginResponse;
import com.futurenbetter.saas.modules.auth.dto.response.SystemAdminRegistrationResponse;
import com.futurenbetter.saas.modules.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/auth")
@RequiredArgsConstructor
public class SystemAdminAuthController {

    private final AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<SystemAdminRegistrationResponse> register(
            @Valid @RequestBody SystemAdminRegistrationRequest request) {
        return ResponseEntity.ok(authService.registerSystemAdmin(request));
    }

    @PostMapping("/login")
    public ResponseEntity<SystemAdminLoginResponse> login(@RequestBody SystemAdminLoginRequest request) {
        return ResponseEntity.ok(authService.loginSystemAdmin(request));
    }
}
