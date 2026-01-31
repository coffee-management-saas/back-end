package com.futurenbetter.saas.modules.employee.service.impl;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.entity.UserProfile;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.auth.repository.UserProfileRepository;
import com.futurenbetter.saas.modules.employee.dto.request.EmployeeRequest;
import com.futurenbetter.saas.modules.employee.dto.response.EmployeeResponse;
import com.futurenbetter.saas.modules.employee.entity.Employee;
import com.futurenbetter.saas.modules.employee.enums.EmployeeStatus;
import com.futurenbetter.saas.modules.employee.mapper.EmployeeMapper;
import com.futurenbetter.saas.modules.employee.repository.EmployeeRepository;
import com.futurenbetter.saas.modules.employee.service.inter.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ShopRepository shopRepository;
    private final UserProfileRepository userProfileRepository;
    private final EmployeeMapper employeeMapper;


    @Override
    public EmployeeResponse getById(Long employeeId) {

        Employee employee = getCurrentEmployee(employeeId);

        return employeeMapper.toResponse(employee);
    }


    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {

        Shop shop = getCurrentShop();
        UserProfile userProfile = getCurrentUserProfile(request.getUserProfileId());

        Employee employee = employeeMapper.toEntity(request);
        employee.setShop(shop);
        employee.setUserProfile(userProfile);

        Employee savedEmployee = employeeRepository.save(employee);

        return employeeMapper.toResponse(savedEmployee);
    }


    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long employeeId, EmployeeRequest request) {

        Employee employee = getCurrentEmployee(employeeId);

        employeeMapper.updateFromRequest(request, employee);

        Employee updatedEmployee = employeeRepository.save(employee);

        return employeeMapper.toResponse(updatedEmployee);
    }


    @Override
    @Transactional
    public void deleteEmployee(Long employeeId) {

        Employee employee = getCurrentEmployee(employeeId);

        employee.setStatus(EmployeeStatus.INACTIVE);

        employeeRepository.save(employee);
    }


    @Override
    public Page<EmployeeResponse> getAll(BaseFilter filter) {

        Long shopId = TenantContext.getCurrentShopId();

        return employeeRepository.findAllByShopId(shopId, filter.getPageable())
                .map(employeeMapper::toResponse);
    }


    private Shop getCurrentShop() {

        Long shopId = TenantContext.getCurrentShopId();
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));

        return shop;
    }


    private UserProfile getCurrentUserProfile(Long userProfileId) {

        UserProfile userProfile = userProfileRepository.findById(userProfileId)
                .orElseThrow(() -> new RuntimeException("UserProfile not found with id: " + userProfileId));

        return userProfile;
    }


    private Employee getCurrentEmployee(Long employeeId) {

        Long shopId = TenantContext.getCurrentShopId();
        Employee employee = employeeRepository.findByIdAndShopId(employeeId, shopId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId + " for shop id: " + shopId));

        return employee;
    }
}
