package com.futurenbetter.saas.modules.inventory.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.inventory.dto.filter.StockCheckSessionFilter;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckApproveRequest;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckItemRequest;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckStartRequest;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckUpdateRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.StockCheckSessionResponse;
import com.futurenbetter.saas.modules.inventory.entity.*;
import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import com.futurenbetter.saas.modules.inventory.enums.TransactionType;
import com.futurenbetter.saas.modules.inventory.mapper.StockCheckMapper;
import com.futurenbetter.saas.modules.inventory.repository.*;
import com.futurenbetter.saas.modules.inventory.service.inter.StockCheckService;
import com.futurenbetter.saas.modules.inventory.specification.StockCheckSessionSpec;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import com.futurenbetter.saas.modules.notification.enums.NotificationType;
import com.futurenbetter.saas.modules.notification.service.inter.NotificationService;
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
    private final StockCheckMapper stockCheckMapper;
    private final NotificationService notificationService;
    private final InventoryTransactionRepository inventoryTransactionRepository;


    @Override
    @Transactional
    public StockCheckSessionResponse startSession(StockCheckStartRequest request) {
        var shop = SecurityUtils.getCurrentShop();
        Long shopAdminId = SecurityUtils.getCurrentUserId();

        // 1. Tạo Session dùng Mapper
        StockCheckSession session = stockCheckMapper.toSessionEntity(request);
        session.setShop(shop);
        session.setCreatedBy(SecurityUtils.getCurrentUserId());
        session.setInventoryStatus(InventoryStatus.ACTIVE);
        session.setCompletedAt(LocalDateTime.now()); // temporary, sẽ update sau khi approve

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
            Double systemStock = batchRepository.sumQuantityByIngredientIdAndStatus(ing.getId(), InventoryStatus.ACTIVE);
            if (systemStock == null) systemStock = 0.0;

            StockCheckDetail detail = new StockCheckDetail();
            detail.setSession(session);
            detail.setIngredient(ing);
            detail.setSnapshotQuantity(systemStock);
            detail.setActualQuantity(0.0);
            detail.setDiffQuantity(0.0);
            detail.setInventoryStatus(InventoryStatus.ACTIVE);

            detailRepository.save(detail);
        }

        Notification noti = Notification.builder()
                .title("Bắt đầu phiên kiểm kê kho")
                .message("Phiên kiểm kho " + session.getCode() + " bắt đầu lúc " + session.getCreatedAt() + ". Vui lòng cập nhật số lượng thực tế.")
                .type(NotificationType.INVENTORY)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/inventory/stock-checks/" + session.getId())
                .shop(shop)
                .build();

        notificationService.sendToUser(noti);

        return getFullResponse(session);
    }


    @Override
    @Transactional
    public StockCheckSessionResponse updateCount(StockCheckUpdateRequest request) {

        Long shopAdminId = SecurityUtils.getCurrentUserId();

        StockCheckSession session = sessionRepository.findByIdAndShopId(request.getSessionId(), SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Phiếu kiểm kê không tồn tại"));

        if (Boolean.TRUE.equals(session.getIsApproved())) {
            throw new BusinessException("Phiếu đã duyệt, không thể sửa");
        }

        for (StockCheckItemRequest itemReq : request.getDetails()) {
            StockCheckDetail detail = detailRepository.findBySessionIdAndIngredientId(session.getId(), itemReq.getIngredientId())
                    .orElseThrow(() -> new BusinessException("Nguyên liệu ID " + itemReq.getIngredientId() + " không có trong phiếu này"));

            // 1. Dùng Mapper để update (actualQuantity, reason)
            stockCheckMapper.updateDetailFromRequest(detail, itemReq);

            // 2. Tính toán diff
            if (detail.getActualQuantity() != null) {
                detail.setDiffQuantity(detail.getActualQuantity() - detail.getSnapshotQuantity());
            }

            detailRepository.save(detail);
        }

        Notification noti = Notification.builder()
                .title("Kiểm kê nguyên liệu đã được cập nhật")
                .message("Số lượng thực tế của các nguyên liệu đã được cập nhật cho phiên kiểm kho " + session.getCode() + ". Vui lòng duyệt phiếu khi đã hoàn tất.")
                .type(NotificationType.INVENTORY)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/inventory/stock-checks/" + session.getId())
                .shop(session.getShop())
                .build();

        notificationService.sendToUser(noti);

        return getFullResponse(session);
    }


    @Override
    @Transactional
    public StockCheckSessionResponse approveSession(StockCheckApproveRequest request) {

        Long shopAdminId = SecurityUtils.getCurrentUserId();

        StockCheckSession session = sessionRepository.findByIdAndShopId(request.getSessionId(), SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Phiếu không tồn tại"));

        if (Boolean.TRUE.equals(request.getIsApproved())) {
            session.setIsApproved(true);
            session.setApprovedBy(SecurityUtils.getCurrentUserId());
            session.setCompletedAt(LocalDateTime.now());
            session.setNote(request.getNote());
            session.setInventoryStatus(InventoryStatus.ACTIVE);

            List<StockCheckDetail> details = detailRepository.findAllBySessionId(session.getId());

            for (StockCheckDetail detail : details) {
                Double diff = detail.getDiffQuantity();

                // Bỏ qua nếu không có sự chênh lệch (Thực tế == Hệ thống)
                if (diff == null || diff == 0.0) {
                    continue;
                }

                RawIngredient ingredient = detail.getIngredient();

                if (diff < 0) {
                    // 1. HAO HỤT (THIẾU HÀNG): Trừ kho theo phương pháp FIFO
                    double amountToDeduct = Math.abs(diff);
                    List<IngredientBatch> activeBatches = batchRepository
                            .findByRawIngredientIdAndInventoryStatusOrderByExpiredAtAsc(ingredient.getId(), InventoryStatus.ACTIVE);

                    for (IngredientBatch batch : activeBatches) {
                        if (amountToDeduct <= 0) break; // Đã trừ đủ số lượng hụt

                        double deductAmount = Math.min(batch.getCurrentQuantity(), amountToDeduct);
                        if (deductAmount > 0) {
                            batch.setCurrentQuantity(batch.getCurrentQuantity() - deductAmount);
                            amountToDeduct -= deductAmount;
                            batchRepository.save(batch);

                            // Ghi lại lịch sử trừ kho
                            createAdjustmentTransaction(session.getShop(), ingredient, batch, detail,
                                    -deductAmount, batch.getCurrentQuantity());
                        }
                    }

                    if (amountToDeduct > 0) {
                        // Cảnh báo: Số lượng hụt lớn hơn cả tổng tồn kho hiện tại (Có thể do sai sót hệ thống nghiêm trọng)
                        // Trong thực tế có thể throw exception hoặc cho phép kho âm. Ở đây ta tạm thời ném lỗi để kiểm soát.
                        throw new BusinessException("Lỗi: Số lượng hụt của " + ingredient.getName() + " lớn hơn tổng tồn các lô!");
                    }

                } else {
                    // 2. DƯ THỪA (THỪA HÀNG): Cộng dồn vào lô có hạn sử dụng dài nhất
                    List<IngredientBatch> activeBatches = batchRepository
                            .findByRawIngredientIdAndInventoryStatusOrderByExpiredAtDesc(ingredient.getId(), InventoryStatus.ACTIVE);

                    if (!activeBatches.isEmpty()) {
                        IngredientBatch latestBatch = activeBatches.get(0);
                        latestBatch.setCurrentQuantity(latestBatch.getCurrentQuantity() + diff);
                        batchRepository.save(latestBatch);

                        // Ghi lại lịch sử cộng kho
                        createAdjustmentTransaction(session.getShop(), ingredient, latestBatch, detail,
                                diff, latestBatch.getCurrentQuantity());
                    } else {
                        // Nếu quán hoàn toàn chưa nhập hàng (không có lô) mà lại kiểm ra thừa hàng
                        throw new BusinessException("Lỗi: Phát hiện dư thừa " + ingredient.getName() + " nhưng nguyên liệu này chưa từng được nhập lô nào. Vui lòng tạo phiếu Nhập Kho trước!");
                    }
                }
            }
            // ==============================================================
            // KẾT THÚC LOGIC CÂN BẰNG KHO
            // ==============================================================

        } else {
            session.setInventoryStatus(InventoryStatus.INACTIVE); // Hủy phiếu
        }

        StockCheckSession result = sessionRepository.save(session);

        // Bắn Notification
        Notification noti = Notification.builder()
                .title("Kiểm kê nguyên liệu hoàn thành")
                .message("Phiên kiểm kho " + session.getCode() + " đã được " + (Boolean.TRUE.equals(request.getIsApproved()) ? "duyệt" : "hủy") + ". Vui lòng kiểm tra lại thẻ kho.")
                .type(NotificationType.INVENTORY)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/inventory/stock-checks/" + session.getId())
                .shop(session.getShop())
                .build();

        notificationService.sendToUser(noti);

        return getFullResponse(result);
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
        var response = stockCheckMapper.toSessionResponse(session);
        // Map list details
        response.setDetails(stockCheckMapper.toDetailResponseList(detailRepository.findAllBySessionId(session.getId())));
        return response;
    }


    private void createAdjustmentTransaction(Shop shop, RawIngredient ingredient, IngredientBatch batch,
                                             StockCheckDetail detail, Double quantityChange, Double quantityAfter) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setShop(shop);
        transaction.setIngredient(ingredient);
        transaction.setBatch(batch);
        transaction.setStockCheckDetail(detail);
        transaction.setQuantityChange(quantityChange);
        transaction.setQuantityAfter(quantityAfter);
        transaction.setTransactionType(TransactionType.STOCK_CHECK_ADJUST);
        transaction.setInventoryStatus(InventoryStatus.ACTIVE);

        inventoryTransactionRepository.save(transaction);
    }
}
