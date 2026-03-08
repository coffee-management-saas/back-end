package com.futurenbetter.saas.modules.auth.service;

import com.futurenbetter.saas.modules.auth.dto.request.*;
import com.futurenbetter.saas.modules.auth.dto.response.*;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthenticationService extends UserDetailsService {
    CustomerResponse register(CustomerRegistrationRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
    void changePassword(Long customerid, ChangePasswordRequest request);
    SystemAdminLoginResponse loginSystemAdmin(SystemAdminLoginRequest request);
    SystemAdminRegistrationResponse registerSystemAdmin(SystemAdminRegistrationRequest request);
    SystemAdminRegistrationResponse registerShopAdmin(ShopAdminRegistrationRequest request);
    ShopEmployeeRegistrationResponse createShopEmployee(ShopEmployeeRegistrationRequest request);
    LoginResponse loginShopAdmin(LoginRequest loginRequest);
    void employeeChangePassword(Long userProfileId, ChangePasswordRequest request);
}
