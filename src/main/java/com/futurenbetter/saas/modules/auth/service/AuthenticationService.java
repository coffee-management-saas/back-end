package com.futurenbetter.saas.modules.auth.service;

import com.futurenbetter.saas.modules.auth.dto.request.*;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.dto.response.LoginResponse;
import com.futurenbetter.saas.modules.auth.dto.response.SystemAdminLoginResponse;
import com.futurenbetter.saas.modules.auth.dto.response.SystemAdminRegistrationResponse;
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
}
