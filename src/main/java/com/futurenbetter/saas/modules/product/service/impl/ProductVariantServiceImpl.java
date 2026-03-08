package com.futurenbetter.saas.modules.product.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import com.futurenbetter.saas.modules.notification.enums.NotificationType;
import com.futurenbetter.saas.modules.notification.service.inter.NotificationService;
import com.futurenbetter.saas.modules.product.dto.filter.ProductVariantFilter;
import com.futurenbetter.saas.modules.product.dto.request.ProductVariantRequest;
import com.futurenbetter.saas.modules.product.dto.response.ProductVariantResponse;
import com.futurenbetter.saas.modules.product.entity.Product;
import com.futurenbetter.saas.modules.product.entity.ProductVariant;
import com.futurenbetter.saas.modules.product.entity.Size;
import com.futurenbetter.saas.modules.product.enums.Status;
import com.futurenbetter.saas.modules.product.mapper.ProductVariantMapper;
import com.futurenbetter.saas.modules.product.repository.ProductRepository;
import com.futurenbetter.saas.modules.product.repository.ProductVariantRepository;
import com.futurenbetter.saas.modules.product.repository.SizeRepository;
import com.futurenbetter.saas.modules.product.service.inter.ProductVariantService;
import com.futurenbetter.saas.modules.product.specification.ProductVariantSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final SizeRepository sizeRepository;
    private final ProductVariantMapper variantMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ProductVariantResponse create(ProductVariantRequest request) {

        Long shopId = SecurityUtils.getCurrentShopId();
        Long shopAdminId = SecurityUtils.getCurrentUserId();

        Product product = productRepository.findByIdAndShopId(request.getProductId(), shopId)
                .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));

        Size size = sizeRepository.findByIdAndShopId(request.getSizeId(), shopId)
                .orElseThrow(() -> new BusinessException("Kích thước không tồn tại"));

        ProductVariant variant = variantMapper.toEntity(request);
        variant.setShop(SecurityUtils.getCurrentShop());
        variant.setProduct(product);
        variant.setSize(size);
        variant.setStatus(Status.ACTIVE);

        ProductVariant result = variantRepository.save(variant);

        Notification noti = Notification.builder()
                .title("Tạo Product Variant thành công")
                .message("Tạo variant cho sản phẩm " + product.getName() + " thành công")
                .type(NotificationType.PRODUCT)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/product/variants/" + result.getId())
                .shop(product.getShop())
                .build();

        notificationService.sendToUser(noti);

        return variantMapper.toResponse(result);
    }

    @Override
    @Transactional
    public ProductVariantResponse update(Long id, ProductVariantRequest request) {

        Long shopId = SecurityUtils.getCurrentShopId();
        Long shopAdminId = SecurityUtils.getCurrentUserId();
        ProductVariant variant = variantRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new BusinessException("Biến thể sản phẩm không tồn tại"));

        // Check SKU nếu đổi
        if (request.getSkuCode() != null && !request.getSkuCode().equals(variant.getSkuCode())) {
            if (variantRepository.existsBySkuCodeAndShopId(request.getSkuCode(), shopId)) {
                throw new BusinessException("Mã SKU đã tồn tại");
            }
        }

        variantMapper.updateFromRequest(variant, request);

        ProductVariant result = variantRepository.save(variant);

        Notification noti = Notification.builder()
                .title("Cập nhật Product Variant thành công")
                .message("Cập nhật variant cho sản phẩm " + variant.getProduct().getName() + " thành công")
                .type(NotificationType.PRODUCT)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/product/variants/" + result.getId())
                .shop(variant.getShop())
                .build();

        notificationService.sendToUser(noti);

        return variantMapper.toResponse(result);
    }

    @Override
    public ProductVariantResponse getDetail(Long id) {
        return variantRepository.findByIdAndShopId(id, SecurityUtils.getCurrentShopId())
                .map(variantMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Biến thể không tồn tại"));
    }

    @Override
    public List<ProductVariantResponse> getByProductId(Long productId) {
        return variantRepository.findAllByProductIdAndShopId(productId, SecurityUtils.getCurrentShopId())
                .stream()
                .map(variantMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductVariantResponse> getAll(ProductVariantFilter filter) {
        return variantRepository.findAll(
                ProductVariantSpec.filter(filter, TenantContext.getCurrentShopId()),
                filter.getPageable()
        ).map(variantMapper::toResponse);
    }
}