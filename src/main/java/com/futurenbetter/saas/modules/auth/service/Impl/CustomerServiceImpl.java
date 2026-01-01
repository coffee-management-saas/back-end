package com.futurenbetter.saas.modules.auth.service.Impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.modules.auth.dto.request.CustomerRegistrationRequest;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.entity.Customer;
import com.futurenbetter.saas.modules.auth.entity.MemberProfile;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.enums.CustomerStatus;
import com.futurenbetter.saas.modules.auth.mapper.CustomerMapper;
import com.futurenbetter.saas.modules.auth.repository.CustomerRepository;
import com.futurenbetter.saas.modules.auth.repository.MemberProfileRepository;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.auth.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final ShopRepository shopRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerResponse register(CustomerRegistrationRequest request) {
        Long currentShopId = TenantContext.getCurrentShopId();

        if (currentShopId == null) {
            throw new BusinessException("Cửa hàng không tồn tại");
        }

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email đã tồn tại");
        }

        if (customerRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }

        Customer customer = customerMapper.toEntity(request);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer = customerRepository.save(customer);

//        MembershipRank defaultRank = rankRepo.findFirstByOrderByMinPointsAsc()
//                .orElseThrow(() -> new BusinessException("Lỗi cấu hình hạng thành viên!"));

        MemberProfile profile = new MemberProfile();
        profile.setCustomer(customer);
        profile.setShop(shopRepository.getReferenceById(currentShopId));
//        profile.setRank(defaultRank);
        profile.setPoint(0);
        profile.setStatus(CustomerStatus.ACTIVE);
        memberProfileRepository.save(profile);

        return customerMapper.toResponse(customer, profile.getId());
    }
}
