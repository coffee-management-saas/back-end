package com.futurenbetter.saas.modules.inventory.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.inventory.dto.filter.RawIngredientFilter;
import com.futurenbetter.saas.modules.inventory.dto.request.RawIngredientRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.RawIngredientResponse;
import com.futurenbetter.saas.modules.inventory.entity.RawIngredient;
import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import com.futurenbetter.saas.modules.inventory.mapper.RawIngredientMapper;
import com.futurenbetter.saas.modules.inventory.repository.IngredientBatchRepository;
import com.futurenbetter.saas.modules.inventory.repository.RawIngredientRepository;
import com.futurenbetter.saas.modules.inventory.service.inter.RawIngredientService;
import com.futurenbetter.saas.modules.inventory.specification.RawIngredientSpec;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import com.futurenbetter.saas.modules.notification.enums.NotificationType;
import com.futurenbetter.saas.modules.notification.service.inter.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RawIngredientServiceImpl implements RawIngredientService {

    private final RawIngredientRepository rawIngredientRepository;
    private final IngredientBatchRepository ingredientBatchRepository;
    private final RawIngredientMapper rawIngredientMapper;
    private final NotificationService notificationService;


    @Override
    @Transactional
    public RawIngredientResponse create(RawIngredientRequest request) {

        Long shopAdminId = SecurityUtils.getCurrentUserId();

        RawIngredient entity = rawIngredientMapper.toEntity(request);
        entity.setShop(SecurityUtils.getCurrentShop());
        entity.setInventoryStatus(InventoryStatus.ACTIVE);

        RawIngredient result = rawIngredientRepository.save(entity);

        Notification noti = Notification.builder()
                .title("Tạo nguyên liệu thành công")
                .message("Tạo nguyên liệu " + request.getName() + " thành công")
                .type(NotificationType.INVENTORY)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/inventory/ingredients/" + result.getId())
                .build();

        notificationService.sendToUser(noti);

        return toFullResponse(result);
    }


    @Override
    @Transactional
    public RawIngredientResponse update(Long id, RawIngredientRequest request) {

        Long shopAdminId = SecurityUtils.getCurrentUserId();

        RawIngredient entity = rawIngredientRepository.findByIdAndShopId(id, SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Nguyên liệu không tồn tại"));

        rawIngredientMapper.updateFromRequest(entity, request);

        RawIngredient result = rawIngredientRepository.save(entity);

        Notification noti = Notification.builder()
                .title("Cập nhật nguyên liệu thành công")
                .message("Cập nhật nguyên liệu " + request.getName() + " thành công")
                .type(NotificationType.INVENTORY)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/inventory/ingredients/" + result.getId())
                .build();

        notificationService.sendToUser(noti);

        return toFullResponse(result);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<RawIngredientResponse> getAll(RawIngredientFilter filter) {
        return rawIngredientRepository.findAll(
                RawIngredientSpec.filter(filter, SecurityUtils.getCurrentShopId()),
                filter.getPageable()
        ).map(this::toFullResponse);
    }


    @Override
    @Transactional(readOnly = true)
    public RawIngredientResponse getDetail(Long id) {
        RawIngredient entity = rawIngredientRepository.findByIdAndShopId(id, SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Nguyên liệu không tồn tại"));
        return toFullResponse(entity);
    }


    private RawIngredientResponse toFullResponse(RawIngredient entity) {
        var res = rawIngredientMapper.toResponse(entity);
        // Tính tổng tồn kho từ Batch (Logic tính toán không thuộc về Mapper)
        Double totalStock = ingredientBatchRepository.sumQuantityByIngredientIdAndStatus(entity.getId(), InventoryStatus.ACTIVE);
        res.setTotalStockQuantity(totalStock != null ? totalStock : 0.0);
        return res;
    }
}
