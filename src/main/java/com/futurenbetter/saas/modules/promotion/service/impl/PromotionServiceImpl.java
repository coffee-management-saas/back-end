package com.futurenbetter.saas.modules.promotion.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.auth.entity.Customer;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.repository.CustomerRepository;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.product.entity.Product;
import com.futurenbetter.saas.modules.product.repository.ProductRepository;
import com.futurenbetter.saas.modules.promotion.dto.request.PromotionRequest;
import com.futurenbetter.saas.modules.promotion.dto.response.PromotionResponse;
import com.futurenbetter.saas.modules.promotion.entity.Promotion;
import com.futurenbetter.saas.modules.promotion.entity.PromotionTarget;
import com.futurenbetter.saas.modules.promotion.entity.PromotionUsage;
import com.futurenbetter.saas.modules.promotion.enums.PromotionEnum;
import com.futurenbetter.saas.modules.promotion.enums.PromotionTypeEnum;
import com.futurenbetter.saas.modules.promotion.enums.PromotionUsageEnum;
import com.futurenbetter.saas.modules.promotion.mapper.PromotionMapper;
import com.futurenbetter.saas.modules.promotion.repository.PromotionRepository;
import com.futurenbetter.saas.modules.promotion.repository.PromotionUsageRepository;
import com.futurenbetter.saas.modules.promotion.service.PromotionService;
import com.futurenbetter.saas.modules.subscription.service.CloudinaryStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final ShopRepository shopRepository;
    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final PromotionUsageRepository promotionUsageRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

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

        if (promotion.getPromotionType() == PromotionTypeEnum.PRODUCT
                && promotionRequest.getProductIds() != null
                && !promotionRequest.getProductIds().isEmpty()) {

            List<PromotionTarget> targets = promotionRequest.getProductIds().stream()
                    .map(productId -> {
                        Product product = productRepository.findById(productId)
                                .orElseThrow(
                                        () -> new BusinessException("Sản phẩm ID " + productId + " không tồn tại"));
                        return PromotionTarget.builder()
                                .promotion(promotion)
                                .shop(shop)
                                .product(product)
                                .build();
                    })
                    .collect(Collectors.toList());

            promotion.setPromotionTargets(targets);
        }

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
            String imageUrl = cloudinaryStorageService.uploadFile(image.getBytes(), fileName, "promotions");

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

    @Override
    @Transactional(readOnly = true)
    public Promotion validatePromotion(String promotionCode, Long shopId, Long customerId, Long orderAmount) {
        Promotion promotion = promotionRepository.findByPromotionCodeAndShopIdWithTargets(promotionCode, shopId)
                .orElseThrow(() -> new BusinessException("Mã khuyến mãi không hợp lệ"));

        if (promotion.getPromotionStatus() != PromotionEnum.ACTIVE) {
            throw new BusinessException("Mã khuyến mãi không còn hiệu lực");
        }

        LocalDateTime now = LocalDateTime.now();
        if (promotion.getStartDate() != null && now.isBefore(promotion.getStartDate())) {
            throw new BusinessException("Mã khuyến mãi chưa bắt đầu");
        }
        if (promotion.getEndDate() != null && now.isAfter(promotion.getEndDate())) {
            throw new BusinessException("Mã khuyến mãi đã hết hạn");
        }

        if (orderAmount < promotion.getMinimumSpent()) {
            throw new BusinessException(
                    String.format("Đơn hàng phải đạt tối thiểu %,d VNĐ để sử dụng mã này",
                            promotion.getMinimumSpent()));
        }

        if (promotion.getQuantity() <= 0) {
            throw new BusinessException("Mã khuyến mãi đã hết lượt sử dụng");
        }

        if (customerId != null && promotion.getUsageLimitPerUser() > 0) {
            long usageCount = promotionUsageRepository.countByPromotionPromotionIdAndCustomerId(
                    promotion.getPromotionId(), customerId);
            if (usageCount >= promotion.getUsageLimitPerUser()) {
                throw new BusinessException("Bạn đã sử dụng hết lượt áp dụng mã này");
            }
        }
        return promotion;
    }

    @Override
    public void recordPromotionUsage(Long promotionId, Long customerId, Long shopId, Long discountAmount) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new BusinessException("Mã khuyến mãi không tồn tại"));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException("Cửa hàng không tồn tại"));

        Customer customer = null;
        if (customerId != null) {
            customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new BusinessException("Khách hàng không tồn tại"));
        }

        PromotionUsage promotionUsage = PromotionUsage.builder()
                .promotion(promotion)
                .customer(customer)
                .shop(shop)
                .discountAmount(discountAmount.floatValue())
                .promotionUsageStatus(PromotionUsageEnum.USED)
                .createdAt(LocalDateTime.now())
                .build();

        promotionUsageRepository.save(promotionUsage);
        promotion.setQuantity(promotion.getQuantity() - 1);
        promotion.setUpdatedAt(LocalDateTime.now());
        promotionRepository.save(promotion);
    }
}
