package com.futurenbetter.saas.modules.inventory.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.inventory.dto.filter.StockCheckSessionFilter;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckApproveRequest;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckItemRequest;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckStartRequest;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckUpdateRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.StockCheckSessionResponse;
import com.futurenbetter.saas.modules.inventory.entity.RawIngredient;
import com.futurenbetter.saas.modules.inventory.entity.StockCheckDetail;
import com.futurenbetter.saas.modules.inventory.entity.StockCheckSession;
import com.futurenbetter.saas.modules.inventory.enums.Status;
import com.futurenbetter.saas.modules.inventory.mapper.StockCheckMapper;
import com.futurenbetter.saas.modules.inventory.repository.IngredientBatchRepository;
import com.futurenbetter.saas.modules.inventory.repository.RawIngredientRepository;
import com.futurenbetter.saas.modules.inventory.repository.StockCheckDetailRepository;
import com.futurenbetter.saas.modules.inventory.repository.StockCheckSessionRepository;
import com.futurenbetter.saas.modules.inventory.service.inter.StockCheckService;
import com.futurenbetter.saas.modules.inventory.specification.StockCheckSessionSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockCheckServiceImpl implements StockCheckService {

    private final StockCheckSessionRepository sessionRepository;
    private final StockCheckDetailRepository detailRepository;
    private final IngredientBatchRepository batchRepository;
    private final RawIngredientRepository ingredientRepository;
    private final StockCheckMapper mapper;


    @Override
    @Transactional
    public StockCheckSessionResponse startSession(StockCheckStartRequest request) {
        var shop = SecurityUtils.getCurrentShop();

        // 1. Tạo Session dùng Mapper
        StockCheckSession session = mapper.toSessionEntity(request);
        session.setShop(shop);
        session.setCreatedBy(SecurityUtils.getCurrentUserId());
        session.setStatus(Status.ACTIVE);

        session = sessionRepository.save(session);

        // 2. Xác định danh sách nguyên liệu cần kiểm
        List<RawIngredient> ingredients;
        if (request.getIngredientIds() == null || request.getIngredientIds().isEmpty()) {
            ingredients = ingredientRepository.findAllByShopId(shop.getId());
        } else {
            ingredients = ingredientRepository.findAllById(request.getIngredientIds());
        }

        // 3. Snapshot tồn kho
        for (RawIngredient ing : ingredients) {
            Double systemStock = batchRepository.sumQuantityByIngredientIdAndStatus(ing.getId(), Status.ACTIVE);
            if (systemStock == null) systemStock = 0.0;

            StockCheckDetail detail = new StockCheckDetail();
            detail.setSession(session);
            detail.setIngredient(ing);
            detail.setSnapshotQuantity(systemStock);
            detail.setActualQuantity(0.0);
            detail.setDiffQuantity(0.0);
            detail.setStatus(Status.ACTIVE);

            detailRepository.save(detail);
        }

        return getFullResponse(session);
    }


    @Override
    @Transactional
    public StockCheckSessionResponse updateCount(StockCheckUpdateRequest request) {
        StockCheckSession session = sessionRepository.findByIdAndShopId(request.getSessionId(), SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Phiếu kiểm kê không tồn tại"));

        if (Boolean.TRUE.equals(session.getIsApproved())) {
            throw new BusinessException("Phiếu đã duyệt, không thể sửa");
        }

        for (StockCheckItemRequest itemReq : request.getDetails()) {
            StockCheckDetail detail = detailRepository.findBySessionIdAndIngredientId(session.getId(), itemReq.getIngredientId())
                    .orElseThrow(() -> new BusinessException("Nguyên liệu ID " + itemReq.getIngredientId() + " không có trong phiếu này"));

            // 1. Dùng Mapper để update (actualQuantity, reason)
            mapper.updateDetailFromRequest(detail, itemReq);

            // 2. Tính toán diff
            if (detail.getActualQuantity() != null) {
                detail.setDiffQuantity(detail.getActualQuantity() - detail.getSnapshotQuantity());
            }

            detailRepository.save(detail);
        }

        return getFullResponse(session);
    }


    @Override
    @Transactional
    public StockCheckSessionResponse approveSession(StockCheckApproveRequest request) {
        StockCheckSession session = sessionRepository.findByIdAndShopId(request.getSessionId(), SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Phiếu không tồn tại"));

        if (Boolean.TRUE.equals(request.getIsApproved())) {
            session.setIsApproved(true);
            session.setApprovedBy(SecurityUtils.getCurrentUserId());
            session.setCompletedAt(LocalDateTime.now());
            session.setNote(request.getNote());
            session.setStatus(Status.ACTIVE);

            // Logic Cân bằng kho (Adjustment) sẽ thực hiện ở đây...
            // (Như đã thảo luận, ta sẽ ghi nhận transaction log hoặc gọi service xử lý batch)
        } else {
            session.setStatus(Status.INACTIVE); // Cancel phiếu
        }

        return getFullResponse(sessionRepository.save(session));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<StockCheckSessionResponse> getAll(StockCheckSessionFilter filter) {
        return sessionRepository.findAll(
                StockCheckSessionSpec.filter(filter, SecurityUtils.getCurrentShopId()),
                filter.getPageable()
        ).map(this::getFullResponse);
    }


    private StockCheckSessionResponse getFullResponse(StockCheckSession session) {
        var response = mapper.toSessionResponse(session);
        // Map list details
        response.setDetails(mapper.toDetailResponseList(detailRepository.findAllBySessionId(session.getId())));
        return response;
    }
}
