package com.futurenbetter.saas.modules.auth.service;

import com.futurenbetter.saas.modules.auth.dto.request.UpdateProfileRequest;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomerService {
    CustomerResponse updateProfile(Long customerId, UpdateProfileRequest request);
}
