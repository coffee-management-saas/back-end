package com.futurenbetter.saas.modules.employee.service.impl;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.modules.employee.dto.request.ScheduleRequest;
import com.futurenbetter.saas.modules.employee.dto.response.ScheduleResponse;
import com.futurenbetter.saas.modules.employee.entity.Employee;
import com.futurenbetter.saas.modules.employee.entity.Schedule;
import com.futurenbetter.saas.modules.employee.enums.ScheduleStatus;
import com.futurenbetter.saas.modules.employee.mapper.ScheduleMapper;
import com.futurenbetter.saas.modules.employee.repository.ScheduleRepository;
import com.futurenbetter.saas.modules.employee.service.inter.EmployeeService;
import com.futurenbetter.saas.modules.employee.service.inter.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final EmployeeService employeeService;
    private final ScheduleMapper scheduleMapper;


    @Override
    @Transactional
    public ScheduleResponse create(ScheduleRequest request) {

        Employee employee = employeeService.getEmployeeById(request.getEmployeeId());

        Schedule schedule = scheduleMapper.toEntity(request);
        schedule.setShop(employee.getShop());
        schedule.setEmployee(employee);

        return scheduleMapper.toResponse(scheduleRepository.save(schedule));
    }


    @Override
    @Transactional
    public ScheduleResponse update(Long id, ScheduleRequest request) {
        Long shopId = TenantContext.getCurrentShopId();

        Schedule schedule = scheduleRepository.findByScheduleIdAndShopId(id, shopId)
                .orElseThrow(() -> new BusinessException("Lịch làm việc không tồn tại"));

        if (request.getEmployeeId() != null && !request.getEmployeeId().equals(schedule.getEmployee().getEmployeeId())) {
            Employee newEmployee = employeeService.getEmployeeById(request.getEmployeeId());
            schedule.setEmployee(newEmployee);
        }

        scheduleMapper.updateFromRequest(request, schedule);
        return scheduleMapper.toResponse(scheduleRepository.save(schedule));
    }


    @Override
    public ScheduleResponse getDetail(Long id) {

        Long shopId = TenantContext.getCurrentShopId();

        return scheduleRepository.findByScheduleIdAndShopId(id, shopId)
                .map(scheduleMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Lịch làm việc không tồn tại"));
    }


    @Override
    @Transactional
    public void delete(Long id) {

        Long shopId = TenantContext.getCurrentShopId();

        Schedule schedule = scheduleRepository.findByScheduleIdAndShopId(id, shopId)
                .orElseThrow(() -> new BusinessException("Lịch làm việc không tồn tại"));

        schedule.setStatus(ScheduleStatus.DELETED);

        scheduleRepository.save(schedule);
    }


    @Override
    public Page<ScheduleResponse> getAll(BaseFilter filter) {

        Long shopId = TenantContext.getCurrentShopId();

        return scheduleRepository.findAllByShopId(shopId, filter.getPageable())
                .map(scheduleMapper::toResponse);
    }


    @Override
    public List<ScheduleResponse> getByEmployeeId(Long employeeId) {

        Long shopId = TenantContext.getCurrentShopId();

        return scheduleRepository.findAllByEmployee_EmployeeIdAndShopId(employeeId, shopId)
                .stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
    }
}