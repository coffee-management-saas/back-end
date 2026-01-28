package com.futurenbetter.saas.modules.promotion.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.promotion.dto.request.PromotionRequest;
import com.futurenbetter.saas.modules.promotion.dto.response.PromotionResponse;
import com.futurenbetter.saas.modules.promotion.entity.Promotion;
import com.futurenbetter.saas.modules.promotion.enums.PromotionEnum;
import com.futurenbetter.saas.modules.promotion.mapper.PromotionMapper;
import com.futurenbetter.saas.modules.promotion.repository.PromotionRepository;
import com.futurenbetter.saas.modules.promotion.service.PromotionService;
import com.futurenbetter.saas.modules.subscription.service.CloudinaryStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.BindException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final ShopRepository shopRepository;
    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;
    private final CloudinaryStorageService cloudinaryStorageService;


    @Override
    public PromotionResponse createPromotion(PromotionRequest promotionRequest) {
        Long currentShopId = SecurityUtils.getCurrentShopId();
        Shop shop = shopRepository.findById(currentShopId)
                .orElseThrow(() -> new BusinessException("Cửa hàng không tồn tại"));

        Promotion promotion = promotionMapper.toEntity(promotionRequest);

        promotion.setShop(shop);
        promotion.setPromotionStatus(PromotionEnum.ACTIVE);
        promotion.setCreatedAt(LocalDateTime.now());
        promotion.setUpdatedAt(LocalDateTime.now());

//        if (promotionRequest.getProductIds() != null && !promotionRequest.getProductIds().isEmpty()) {
//            List<PromotionTarget> targets = promotionRequest.getProductIds().stream()
//                    .map(productId -> PromotionTarget.builder()
//                            .promotion(promotion)
//                            .shop(shop)
//                            .productId(productId)
//                            .build())
//                    .collect(Collectors.toList());
//            promotion.setPromotionTargets(targets);
//        }

        Promotion savedPromotion = promotionRepository.save(promotion);
        return promotionMapper.toResponse(savedPromotion);
    }

    @Override
    public PromotionResponse uploadImage(Long promotionId, MultipartFile image) throws IOException {
        Long currentShopId = SecurityUtils.getCurrentShopId();
        Promotion promotion = promotionRepository.findById(promotionId)
                .filter(p -> p.getShop().getId().equals(currentShopId))
                .orElseThrow(() -> new BusinessException("Mã khuyến mãi không tồn tại"));

        if (image != null && !image.isEmpty()) {
            String fileName = "promotion_" + promotionId + "_" + System.currentTimeMillis();
            String imageUrl = cloudinaryStorageService.uploadInvoice(image.getBytes(), fileName); //

            promotion.setImageUrl(imageUrl);
            promotion.setUpdatedAt(LocalDateTime.now());

            Promotion savedPromotion = promotionRepository.save(promotion);
            return promotionMapper.toResponse(savedPromotion);
        } else {
            throw new BusinessException("Vui lòng chọn file ảnh để upload");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<PromotionResponse> getAllPromotions() {
        Long currentShopId = TenantContext.getCurrentShopId();

        if (currentShopId == null) {
            throw new BusinessException("Không tìm thấy cửa hàng");
        }

        return promotionRepository.findAllByShopIdAndPromotionStatus(
                        currentShopId, PromotionEnum.ACTIVE)
                .stream()
                .map(promotionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PromotionResponse getPromotion(Long promotionId) {
        Long currentShopId = TenantContext.getCurrentShopId();

        if (currentShopId == null) {
            throw new BusinessException("Không tìm thấy cửa hàng");
        }

        return promotionRepository.findById(promotionId)
                .filter(p -> p.getShop().getId().equals(currentShopId))
                .map(promotionMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Mã khuyến mãi không tồn tại"));
    }

    @Override
    public PromotionResponse updatePromotion(Long promotionId, PromotionRequest promotionRequest) {
        Long currentShopId = TenantContext.getCurrentShopId();

        Promotion promotion = promotionRepository.findById(promotionId)
                .filter(p -> p.getShop().getId().equals(currentShopId))
                .orElseThrow(() -> new BusinessException("Mã khuyến mãi không tồn tại"));

        promotionMapper.updateEntity(promotionRequest, promotion);
        promotion.setUpdatedAt(LocalDateTime.now());
        return promotionMapper.toResponse(promotionRepository.save(promotion));
    }

    @Override
    public void deletePromotion(Long promotionId) {
        Long currentShopId = TenantContext.getCurrentShopId();

        Promotion promotion = promotionRepository.findById(promotionId)
                .filter(p -> p.getShop().getId().equals(currentShopId))
                .orElseThrow(() -> new BusinessException("Mã khuyến mãi không tồn tại"));

        promotion.setPromotionStatus(PromotionEnum.INACTIVE);
        promotion.setUpdatedAt(LocalDateTime.now());
        promotionRepository.save(promotion);
    }
}
