package com.futurenbetter.saas.modules.product.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import com.futurenbetter.saas.modules.notification.enums.NotificationType;
import com.futurenbetter.saas.modules.notification.service.inter.NotificationService;
import com.futurenbetter.saas.modules.product.dto.request.SizeRequest;
import com.futurenbetter.saas.modules.product.dto.response.SizeResponse;
import com.futurenbetter.saas.modules.product.entity.Size;

import com.futurenbetter.saas.modules.product.enums.SizeStatus;
import com.futurenbetter.saas.modules.product.enums.Status;
import com.futurenbetter.saas.modules.product.mapper.SizeMapper;
import com.futurenbetter.saas.modules.product.repository.SizeRepository;
import com.futurenbetter.saas.modules.product.service.inter.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SizeServiceImpl implements SizeService {

    private final SizeRepository sizeRepository;
    private final SizeMapper sizeMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public SizeResponse create(SizeRequest request) {

        Long shopId = TenantContext.getCurrentShopId();
        Long shopAdminId = SecurityUtils.getCurrentUserId();

        if (sizeRepository.existsByCodeAndShopId(request.getCode(), shopId)) {
            throw new BusinessException("Mã kích thước đã tồn tại");
        }

        Size size = sizeMapper.toEntity(request);
        size.setShop(SecurityUtils.getCurrentShop());

        Size result = sizeRepository.save(size);

        Notification noti = Notification.builder()
                .title("Tạo Size thành công")
                .message("Tạo Size " + result.getCode() + " thành công")
                .type(NotificationType.PRODUCT)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/product/sizes/" + result.getId())
                .build();

        notificationService.sendToUser(noti);

        return sizeMapper.toResponse(result);
    }

    @Override
    @Transactional
    public SizeResponse update(Long id, SizeRequest request) {

        Long shopAdminId = SecurityUtils.getCurrentUserId();
        Size size = sizeRepository.findByIdAndShopId(id, TenantContext.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Kích thước không tồn tại"));

        if (request.getCode() != null && !request.getCode().equals(size.getCode())) {
            if (sizeRepository.existsByCodeAndShopId(request.getCode(), TenantContext.getCurrentShopId())) {
                throw new BusinessException("Mã kích thước đã tồn tại");
            }
        }

        sizeMapper.updateFromRequest(size, request);

        Size result = sizeRepository.save(size);

        Notification noti = Notification.builder()
                .title("Tạo Size thành công")
                .message("Tạo Size " + result.getCode() + " thành công")
                .type(NotificationType.PRODUCT)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/product/sizes/" + result.getId())
                .build();

        notificationService.sendToUser(noti);

        return sizeMapper.toResponse(result);
    }

    @Override
    public List<SizeResponse> getAll(SizeStatus status) {

        Long shopId = TenantContext.getCurrentShopId();
        List<Size> sizes;
        
        if (status != null) {
            Status entityStatus = Status.valueOf(status.name());
            sizes = sizeRepository.findAllByShopIdAndStatus(shopId, entityStatus);
        } else {
            sizes = sizeRepository.findAllByShopId(shopId);
        }

        return sizes.stream()
                .map(sizeMapper::toResponse)
                .toList();
    }

    @Override
    public List<SizeResponse> getActiveSizes() {
        return sizeRepository.findAll().stream()
                .filter(size -> size.getStatus() == Status.ACTIVE)
                .map(sizeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {

        Long shopAdminId = SecurityUtils.getCurrentUserId();
        Size size = sizeRepository.findByIdAndShopId(id, TenantContext.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Kích thước không tồn tại"));

        size.setStatus(Status.DELETED);
        sizeRepository.save(size);

        Notification noti = Notification.builder()
                .title("Xóa Size thành công")
                .message("Xóa Size " + size.getCode() + " thành công")
                .type(NotificationType.PRODUCT)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/product/sizes/" + size.getId())
                .build();

        notificationService.sendToUser(noti);
    }
}