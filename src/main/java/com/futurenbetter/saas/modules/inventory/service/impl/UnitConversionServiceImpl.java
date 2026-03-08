package com.futurenbetter.saas.modules.inventory.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.inventory.dto.request.UnitConversionRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.UnitConversionResponse;
import com.futurenbetter.saas.modules.inventory.entity.UnitConversion;
import com.futurenbetter.saas.modules.inventory.enums.InputUnit;
import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import com.futurenbetter.saas.modules.inventory.mapper.UnitConversionMapper;
import com.futurenbetter.saas.modules.inventory.repository.RawIngredientRepository;
import com.futurenbetter.saas.modules.inventory.repository.UnitConversionRepository;
import com.futurenbetter.saas.modules.inventory.service.inter.UnitConversionService;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import com.futurenbetter.saas.modules.notification.enums.NotificationType;
import com.futurenbetter.saas.modules.notification.service.inter.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnitConversionServiceImpl implements UnitConversionService {

    private final UnitConversionRepository unitConversionRepository;
    private final RawIngredientRepository ingredientRepository;
    private final UnitConversionMapper unitConversionMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public UnitConversionResponse create(UnitConversionRequest request) {
        Long shopId = SecurityUtils.getCurrentShopId();
        Long shopAdminId = SecurityUtils.getCurrentUserId();
        var ingredient = ingredientRepository
                .findByIdAndShopId(request.getIngredientId(), shopId)
                .orElseThrow(() -> new BusinessException("Nguyên liệu không tồn tại"));

        if (unitConversionRepository.existsByIngredientIdAndFromUnitAndInventoryStatus(ingredient.getId(), request.getFromUnit(),
                InventoryStatus.ACTIVE)) {
            throw new BusinessException("Đơn vị " + request.getFromUnit() + " đã được cấu hình cho nguyên liệu này");
        }

        UnitConversion entity = unitConversionMapper.toEntity(request);
        entity.setShop(SecurityUtils.getCurrentShop());
        entity.setIngredient(ingredient);
        entity.setInventoryStatus(InventoryStatus.ACTIVE);

        entity = unitConversionRepository.save(entity);

        Notification noti = Notification.builder()
                .title("Tạo bảng quy đổi thành công")
                .message("Tạo bảng quy đổi từ " + request.getFromUnit() + " sang " + request.getToUnit() + " với hệ số " + request.getConversionFactor() + " cho nguyên liệu " + ingredient.getName() + " thành công")
                .type(NotificationType.INVENTORY)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/inventory/unit-conversions/" + entity.getId())
                .shop(entity.getShop())
                .build();

        notificationService.sendToUser(noti);

        return unitConversionMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public UnitConversion update(Long id, UnitConversionRequest request) {

        Long shopAdminId = SecurityUtils.getCurrentUserId();

        UnitConversion entity = unitConversionRepository.findByIdAndShopId(id, SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Cấu hình quy đổi không tồn tại"));

        unitConversionMapper.updateFromRequest(entity, request);

        UnitConversion result = unitConversionRepository.save(entity);

        Notification noti = Notification.builder()
                .title("Cập nhật bảng quy đổi thành công")
                .message("Cập nhật bảng quy đổi từ " + request.getFromUnit() + " sang " + request.getToUnit() + " với hệ số " + request.getConversionFactor() + " cho nguyên liệu " + entity.getIngredient().getName() + " thành công")
                .type(NotificationType.INVENTORY)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/inventory/unit-conversions/" + result.getId())
                .shop(entity.getShop())
                .build();

        notificationService.sendToUser(noti);

        return unitConversionRepository.save(result);
    }

    @Override
    public Double convertToBaseUnit(Long ingredientId, InputUnit fromUnit, Double quantity) {
        return unitConversionRepository
                .findByIngredientIdAndFromUnitAndInventoryStatus(ingredientId, fromUnit, InventoryStatus.ACTIVE)
                .map(conversion -> quantity * conversion.getConversionFactor())
                .orElseThrow(() -> new BusinessException("Chưa cấu hình quy đổi cho đơn vị: " + fromUnit));
    }
}