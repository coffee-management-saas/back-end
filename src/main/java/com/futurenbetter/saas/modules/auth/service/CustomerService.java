package com.futurenbetter.saas.modules.auth.service;

import com.futurenbetter.saas.modules.auth.dto.request.CustomerRegistrationRequest;
import com.futurenbetter.saas.modules.auth.dto.request.LoginRequest;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.dto.response.LoginResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomerService extends UserDetailsService {
    CustomerResponse register(CustomerRegistrationRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(String token);
    void logout(String token);
}
