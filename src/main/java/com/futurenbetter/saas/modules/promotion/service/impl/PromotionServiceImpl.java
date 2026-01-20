package com.futurenbetter.saas.modules.promotion.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.promotion.dto.request.PromotionRequest;
import com.futurenbetter.saas.modules.promotion.dto.response.PromotionResponse;
import com.futurenbetter.saas.modules.promotion.entity.Promotion;
import com.futurenbetter.saas.modules.promotion.enums.PromotionEnum;
import com.futurenbetter.saas.modules.promotion.mapper.PromotionMapper;
import com.futurenbetter.saas.modules.promotion.repository.PromotionRepository;
import com.futurenbetter.saas.modules.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    @Override
    public PromotionResponse createPromotion(PromotionRequest promotionRequest) {
        Shop shop = shopRepository.findById(promotionRequest.getShopId())
                .orElseThrow(() -> new BusinessException("Cửa hàng không tồn tại"));

        Promotion promotion = promotionMapper.toEntity(promotionRequest);

        promotion.setShop(shop);
        promotion.setPromotionStatus(PromotionEnum.ACTIVE);
        promotion.setImageUrl(promotionRequest.getImageUrl());
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
