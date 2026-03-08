package com.futurenbetter.saas.modules.auth.service;

import com.futurenbetter.saas.modules.auth.dto.filter.CustomerFilter;
import com.futurenbetter.saas.modules.auth.dto.request.UpdateProfileRequest;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

public interface CustomerService {
    CustomerResponse getCustomer(Customer customer);
    CustomerResponse updateProfile(Long customerId, UpdateProfileRequest request);
    Integer countNewCustomers(Long shopId, LocalDateTime start, LocalDateTime end);
    Integer countReturningCustomers(Long shopId, LocalDateTime start, LocalDateTime end);
    Page<CustomerResponse> getCustomers(CustomerFilter filter);
    CustomerResponse deleteCustomer(Long customerId);
    CustomerResponse getCustomerById(Long customerId);
}
