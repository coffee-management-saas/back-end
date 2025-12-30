package com.futurenbetter.saas.modules.auth.service;

import com.futurenbetter.saas.modules.auth.dto.request.CustomerRegistrationRequest;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import lombok.RequiredArgsConstructor;

public interface CustomerService {
    CustomerResponse register(CustomerRegistrationRequest request);
}
