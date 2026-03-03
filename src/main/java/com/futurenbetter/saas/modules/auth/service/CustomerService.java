package com.futurenbetter.saas.modules.auth.service;

import com.futurenbetter.saas.modules.auth.dto.request.UpdateProfileRequest;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.entity.Customer;

import java.time.LocalDateTime;

public interface CustomerService {
    CustomerResponse getCustomer(Customer customer);
    CustomerResponse updateProfile(Long customerId, UpdateProfileRequest request);
    Integer countNewCustomers(Long shopId, LocalDateTime start, LocalDateTime end);
    Integer countReturningCustomers(Long shopId, LocalDateTime start, LocalDateTime end);
}
