package com.futurenbetter.saas.modules.auth.service.Impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.modules.auth.dto.request.UpdateProfileRequest;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.entity.Customer;
import com.futurenbetter.saas.modules.auth.mapper.CustomerMapper;
import com.futurenbetter.saas.modules.auth.repository.CustomerRepository;
import com.futurenbetter.saas.modules.auth.service.CustomerService;
import com.futurenbetter.saas.modules.order.entity.Order;
import com.futurenbetter.saas.modules.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Customer customer) {
        return customerMapper.toResponse(customer);
    }

    @Override
    public CustomerResponse updateProfile(Long customerId, UpdateProfileRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng"));

        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email đã được sử dụng");
            }
        }

        customerMapper.updateCustomerRequest(request, customer);
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);
        return customerMapper.toResponse(customer);
    }

    @Override
    public Integer countNewCustomers(Long shopId, LocalDateTime start, LocalDateTime end) {
        return customerRepository.countByShopIdAndCreatedAtBetween(shopId, start, end);
    }

    @Override
    public Integer countReturningCustomers(Long shopId, LocalDateTime start, LocalDateTime end) {
        Month currentMonth = start.getMonth();
        List<Customer> rawCount = customerRepository.findCustomersWithMinOrders(shopId, 1, start, end);
        Integer total = 0;
        for (Customer customer : rawCount) {
            Order order = customer.getOrders().getLast();
            if(order.getCreatedAt().getMonth() == currentMonth) {
                total++;
            }
        }
        return total;
    }
}
