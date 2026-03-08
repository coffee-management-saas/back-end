package com.futurenbetter.saas.modules.employee.service.impl;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.employee.dto.request.ShiftTemplateRequest;
import com.futurenbetter.saas.modules.employee.dto.response.ShiftTemplateResponse;
import com.futurenbetter.saas.modules.employee.entity.ShiftTemplate;
import com.futurenbetter.saas.modules.employee.enums.ShiftTemplateStatus;
import com.futurenbetter.saas.modules.employee.mapper.ShiftTemplateMapper;
import com.futurenbetter.saas.modules.employee.repository.ShiftTemplateRepository;
import com.futurenbetter.saas.modules.employee.service.inter.ShiftTemplateService;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import com.futurenbetter.saas.modules.notification.enums.NotificationType;
import com.futurenbetter.saas.modules.notification.service.inter.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShiftTemplateServiceImpl implements ShiftTemplateService {

    private final ShiftTemplateRepository shiftTemplateRepository;
    private final ShiftTemplateMapper shiftTemplateMapper;
    private final NotificationService notificationService;


    @Override
    @Transactional
    public ShiftTemplateResponse create(ShiftTemplateRequest request) {

        Shop currentShop = SecurityUtils.getCurrentShop();
        Long shopAdminId = SecurityUtils.getCurrentUserId();

        ShiftTemplate template = shiftTemplateMapper.toEntity(request);
        template.setShop(currentShop);
        template.setStatus(ShiftTemplateStatus.ACTIVE);

        ShiftTemplate result = shiftTemplateRepository.save(template);

        Notification noti = Notification.builder()
                .title("Tạo ca làm thành công")
                .message("Bạn đã tạo ca làm " + request.getName() + " thành công . Thời gian từ " + request.getStartTime() + " đến " + request.getEndTime())
                .type(NotificationType.SCHEDULE)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/employee/shift-templates/" + result.getShiftTemplateId())
                .build();

        notificationService.sendToUser(noti);

        return shiftTemplateMapper.toResponse(result);
    }


    @Override
    @Transactional
    public ShiftTemplateResponse update(Long id, ShiftTemplateRequest request) {

        Long shopId = TenantContext.getCurrentShopId();
        Long shopAdminId = SecurityUtils.getCurrentUserId();

        ShiftTemplate template = shiftTemplateRepository.findByShiftTemplateIdAndShopId(id, shopId)
                .orElseThrow(() -> new BusinessException("Mẫu ca làm việc không tồn tại"));

        shiftTemplateMapper.updateFromRequest(request, template);

        ShiftTemplate result = shiftTemplateRepository.save(template);

        Notification noti = Notification.builder()
                .title("Cập nhật ca làm thành công")
                .message("Bạn đã cập nhật ca làm " + request.getName() + " thành công . Thời gian từ " + request.getStartTime() + " đến " + request.getEndTime())
                .type(NotificationType.SCHEDULE)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/employee/shift-templates/" + result.getShiftTemplateId())
                .build();

        notificationService.sendToUser(noti);

        return shiftTemplateMapper.toResponse(result);
    }


    @Override
    public ShiftTemplateResponse getDetail(Long id) {

        Long shopId = TenantContext.getCurrentShopId();

        return shiftTemplateRepository.findByShiftTemplateIdAndShopId(id, shopId)
                .map(shiftTemplateMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Mẫu ca làm việc không tồn tại"));
    }


    @Override
    @Transactional
    public void delete(Long id) {

        Long shopId = TenantContext.getCurrentShopId();
        Long shopAdminId = SecurityUtils.getCurrentUserId();
        ShiftTemplate template = shiftTemplateRepository.findByShiftTemplateIdAndShopId(id, shopId)
                .orElseThrow(() -> new BusinessException("Mẫu ca làm việc không tồn tại"));

        template.setStatus(ShiftTemplateStatus.INACTIVE);

        shiftTemplateRepository.save(template);

        Notification noti = Notification.builder()
                .title("Cập nhật ca làm thành công")
                .message("Bạn đã cập nhật ca làm " + template.getName() + " thành công . Thời gian từ " + template.getStartTime() + " đến " + template.getEndTime())
                .type(NotificationType.SCHEDULE)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/employee/shift-templates/" + template.getShiftTemplateId())
                .build();

        notificationService.sendToUser(noti);
    }


    @Override
    public Page<ShiftTemplateResponse> getAll(BaseFilter filter) {

        Long shopId = TenantContext.getCurrentShopId();

        return shiftTemplateRepository.findAllByShopId(shopId, filter.getPageable())
                .map(shiftTemplateMapper::toResponse);
    }
}