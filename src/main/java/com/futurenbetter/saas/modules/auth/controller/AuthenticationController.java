package com.futurenbetter.saas.modules.auth.controller;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.modules.auth.dto.request.*;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.dto.response.LoginResponse;
import com.futurenbetter.saas.modules.auth.dto.response.SystemAdminRegistrationResponse;
import com.futurenbetter.saas.modules.auth.entity.Customer;
import com.futurenbetter.saas.modules.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")   
    public ResponseEntity<CustomerResponse> register(@RequestBody CustomerRegistrationRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    //nên lưu refresh token vào database để quản lý việc đăng xuất của user
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody TokenRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody TokenRequest request) {
        authenticationService.logout(request.getRefreshToken());
        return ResponseEntity.ok("Đăng xuất thành công");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal Customer customer,
                                                 @RequestBody ChangePasswordRequest request) {
        if (customer == null) {
            throw new BusinessException("Phiên đăng nhập hết hạn");
        }

        authenticationService.changePassword(customer.getId(), request);
        return ResponseEntity.ok("Thay đổi mật khẩu thành công");
    }

    @PostMapping("/shop-admin/register")
    public ResponseEntity<SystemAdminRegistrationResponse> registerShopAdmin(@RequestBody ShopAdminRegistrationRequest request) {
        SystemAdminRegistrationResponse response = authenticationService.registerShopAdmin(request);
        return ResponseEntity.ok(response);
    }

}
