package com.futurenbetter.saas.modules.employee.service.impl;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.modules.employee.dto.request.EmployeeUnavailabilityRequest;
import com.futurenbetter.saas.modules.employee.dto.response.EmployeeUnavailabilityResponse;
import com.futurenbetter.saas.modules.employee.entity.Employee;
import com.futurenbetter.saas.modules.employee.entity.EmployeeUnavailability;
import com.futurenbetter.saas.modules.employee.enums.UnavailabilityStatus;
import com.futurenbetter.saas.modules.employee.mapper.EmployeeUnavailabilityMapper;
import com.futurenbetter.saas.modules.employee.repository.EmployeeUnavailabilityRepository;
import com.futurenbetter.saas.modules.employee.service.inter.EmployeeService;
import com.futurenbetter.saas.modules.employee.service.inter.EmployeeUnavailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeUnavailabilityServiceImpl implements EmployeeUnavailabilityService {

    private final EmployeeUnavailabilityRepository unavailabilityRepository;
    private final EmployeeService employeeService;
    private final EmployeeUnavailabilityMapper mapper;


    @Override
    @Transactional
    public EmployeeUnavailabilityResponse create(EmployeeUnavailabilityRequest request) {

        Employee employee = employeeService.getEmployeeById(request.getEmployeeId());

        EmployeeUnavailability entity = mapper.toEntity(request);
        entity.setShop(employee.getShop());
        entity.setEmployee(employee);

        EmployeeUnavailability result = unavailabilityRepository.save(entity);

        return mapper.toResponse(result);
    }


    @Override
    @Transactional
    public EmployeeUnavailabilityResponse update(Long id, EmployeeUnavailabilityRequest request) {

        Long shopId = TenantContext.getCurrentShopId();
        EmployeeUnavailability entity = unavailabilityRepository.findByEmployeeUnavailabilityIdAndShopId(id, shopId)
                .orElseThrow(() -> new BusinessException("Thông tin không tồn tại"));

        mapper.updateFromRequest(request, entity);

        EmployeeUnavailability result = unavailabilityRepository.save(entity);

        return mapper.toResponse(result);
    }


    @Override
    public EmployeeUnavailabilityResponse getDetail(Long id) {

        Long shopId = TenantContext.getCurrentShopId();

        return unavailabilityRepository.findByEmployeeUnavailabilityIdAndShopId(id, shopId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new BusinessException("Thông tin không tồn tại"));
    }


    @Override
    @Transactional
    public void delete(Long id) {

        Long shopId = TenantContext.getCurrentShopId();
        EmployeeUnavailability entity = unavailabilityRepository.findByEmployeeUnavailabilityIdAndShopId(id, shopId)
                .orElseThrow(() -> new BusinessException("Thông tin không tồn tại"));

        entity.setStatus(UnavailabilityStatus.INACTIVE);
        unavailabilityRepository.save(entity);
    }


    @Override
    public Page<EmployeeUnavailabilityResponse> getAll(BaseFilter filter) {

        Long shopId = TenantContext.getCurrentShopId();

        return unavailabilityRepository.findAllByShopId(shopId, filter.getPageable())
                .map(mapper::toResponse);
    }
}