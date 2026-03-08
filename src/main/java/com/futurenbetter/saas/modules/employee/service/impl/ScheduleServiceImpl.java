package com.futurenbetter.saas.modules.employee.service.impl;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.employee.dto.request.ScheduleRequest;
import com.futurenbetter.saas.modules.employee.dto.response.ScheduleResponse;
import com.futurenbetter.saas.modules.employee.entity.Employee;
import com.futurenbetter.saas.modules.employee.entity.Schedule;
import com.futurenbetter.saas.modules.employee.enums.ScheduleStatus;
import com.futurenbetter.saas.modules.employee.mapper.ScheduleMapper;
import com.futurenbetter.saas.modules.employee.repository.ScheduleRepository;
import com.futurenbetter.saas.modules.employee.service.inter.EmployeeService;
import com.futurenbetter.saas.modules.employee.service.inter.ScheduleService;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import com.futurenbetter.saas.modules.notification.enums.NotificationType;
import com.futurenbetter.saas.modules.notification.service.inter.NotificationService;
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
    private final NotificationService notificationService;


    @Override
    @Transactional
    public ScheduleResponse create(ScheduleRequest request) {

        Employee employee = employeeService.getEmployeeById(request.getEmployeeId());
        Long shopAdminId = SecurityUtils.getCurrentUserId();

        Schedule schedule = scheduleMapper.toEntity(request);
        schedule.setShop(employee.getShop());
        schedule.setEmployee(employee);

        Schedule result = scheduleRepository.save(schedule);

        Notification noti = Notification.builder()
                .title("Xếp lịch làm việc thành công")
                .message("Bạn đã xếp lịch làm việc cho " + employee.getUserProfile().getFullname() +  " từ " + request.getStartTime() + " đến " + request.getEndTime())
                .type(NotificationType.SCHEDULE)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/employee/schedules/" + result.getScheduleId())
                .build();

        notificationService.sendToUser(noti);

        return scheduleMapper.toResponse(result);
    }


    @Override
    @Transactional
    public ScheduleResponse update(Long id, ScheduleRequest request) {

        Long shopId = SecurityUtils.getCurrentShopId();
        Long shopAdminId = SecurityUtils.getCurrentUserId();

        Schedule schedule = scheduleRepository.findByScheduleIdAndShopId(id, shopId)
                .orElseThrow(() -> new BusinessException("Lịch làm việc không tồn tại"));

        if (request.getEmployeeId() != null && !request.getEmployeeId().equals(schedule.getEmployee().getEmployeeId())) {
            Employee newEmployee = employeeService.getEmployeeById(request.getEmployeeId());
            schedule.setEmployee(newEmployee);
        }

        scheduleMapper.updateFromRequest(request, schedule);
        Schedule result = scheduleRepository.save(schedule);

        Notification noti = Notification.builder()
                .title("Cập nhật lịch làm việc thành công")
                .message("Bạn đã cập nhật lịch làm việc cho " + result.getEmployee().getUserProfile().getFullname() +  " từ " + request.getStartTime() + " đến " + request.getEndTime())
                .type(NotificationType.SCHEDULE)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/employee/schedules/" + result.getScheduleId())
                .build();

        notificationService.sendToUser(noti);

        return scheduleMapper.toResponse(result);
    }


    @Override
    public ScheduleResponse getDetail(Long id) {

        Long shopId = SecurityUtils.getCurrentShopId();

        return scheduleRepository.findByScheduleIdAndShopId(id, shopId)
                .map(scheduleMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Lịch làm việc không tồn tại"));
    }


    @Override
    @Transactional
    public void delete(Long id) {

        Long shopId = SecurityUtils.getCurrentShopId();
        Long shopAdminId = SecurityUtils.getCurrentUserId();

        Schedule schedule = scheduleRepository.findByScheduleIdAndShopId(id, shopId)
                .orElseThrow(() -> new BusinessException("Lịch làm việc không tồn tại"));

        schedule.setStatus(ScheduleStatus.DELETED);

        scheduleRepository.save(schedule);

        Notification noti = Notification.builder()
                .title("Xóa lịch làm việc thành công")
                .message("Bạn đã xóa lịch làm việc cho " + schedule.getEmployee().getUserProfile().getFullname() +  " từ " + schedule.getStartTime() + " đến " + schedule.getEndTime())
                .type(NotificationType.SCHEDULE)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/employee/schedules/" + schedule.getScheduleId())
                .build();

        notificationService.sendToUser(noti);
    }


    @Override
    public Page<ScheduleResponse> getAll(BaseFilter filter) {

        Long shopId = SecurityUtils.getCurrentShopId();

        return scheduleRepository.findAllByShopId(shopId, filter.getPageable())
                .map(scheduleMapper::toResponse);
    }


    @Override
    public List<ScheduleResponse> getByEmployeeId(Long employeeId) {

        Long shopId = SecurityUtils.getCurrentShopId();

        return scheduleRepository.findAllByEmployee_EmployeeIdAndShopId(employeeId, shopId)
                .stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
    }
}