package com.futurenbetter.saas.modules.product.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.product.dto.filter.ProductFilter;
import com.futurenbetter.saas.modules.product.dto.request.ProductRequest;
import com.futurenbetter.saas.modules.product.dto.response.ProductResponse;
import com.futurenbetter.saas.modules.product.entity.Category;
import com.futurenbetter.saas.modules.product.entity.Product;
import com.futurenbetter.saas.modules.product.entity.ProductAllowTopping;
import com.futurenbetter.saas.modules.product.entity.Topping;
import com.futurenbetter.saas.modules.product.enums.Status;
import com.futurenbetter.saas.modules.product.mapper.ProductMapper;
import com.futurenbetter.saas.modules.product.repository.CategoryRepository;
import com.futurenbetter.saas.modules.product.repository.ProductAllowToppingRepository;
import com.futurenbetter.saas.modules.product.repository.ProductRepository;
import com.futurenbetter.saas.modules.product.repository.ToppingRepository;
import com.futurenbetter.saas.modules.product.service.inter.ProductService;
import com.futurenbetter.saas.modules.product.specification.ProductSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductAllowToppingRepository allowToppingRepository;
    private final ToppingRepository toppingRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Long currentShopId = TenantContext.getCurrentShopId();

        if (productRepository.existsByNameAndShopId(request.getName(), currentShopId)) {
            throw new BusinessException("Tên sản phẩm đã tồn tại");
        }

        Category category = categoryRepository.findByIdAndShopId(request.getCategoryId(), currentShopId)
                .orElseThrow(() -> new BusinessException("Danh mục không tồn tại"));

        Product product = productMapper.toEntity(request);
        product.setShop(SecurityUtils.getCurrentShop());
        product.setCategory(category);
        product.setStatus(Status.ACTIVE);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Long currentShopId = TenantContext.getCurrentShopId();
        Product product = productRepository.findByIdAndShopId(id, currentShopId)
                .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));

        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategory().getId())) {
            Category newCategory = categoryRepository.findByIdAndShopId(request.getCategoryId(), currentShopId)
                    .orElseThrow(() -> new BusinessException("Danh mục mới không tồn tại"));
            product.setCategory(newCategory);
        }

        productMapper.updateFromRequest(product, request);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse getDetail(Long id) {
        Long currentShopId = TenantContext.getCurrentShopId();
        return productRepository.findByIdAndShopId(id, currentShopId)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));
    }

    @Override
    public Page<ProductResponse> getAll(ProductFilter filter) {
        Long currentShopId = TenantContext.getCurrentShopId();
        return productRepository.findAll(
                ProductSpec.filter(filter, currentShopId),
                filter.getPageable()
        ).map(productMapper::toResponse);
    }

    @Override
    @Transactional
    public void updateAllowToppings(Long productId, List<Long> toppingIds) {
        Long currentShopId = TenantContext.getCurrentShopId();
        Product product = productRepository.findByIdAndShopId(productId, currentShopId)
                .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));

        allowToppingRepository.deleteByProductId(productId);

        if (toppingIds != null && !toppingIds.isEmpty()) {
            List<Topping> toppings = toppingRepository.findAllById(toppingIds);

            List<ProductAllowTopping> newMappings = toppings.stream().map(t -> {
                ProductAllowTopping mapping = new ProductAllowTopping();
                mapping.setProduct(product);
                mapping.setTopping(t);
                mapping.setStatus(Status.ACTIVE);
                return mapping;
            }).collect(Collectors.toList());

            allowToppingRepository.saveAll(newMappings);
        }
    }

    @Override
    public List<Long> getAllowToppingIds(Long productId) {
        Long currentShopId = TenantContext.getCurrentShopId();
        return allowToppingRepository.findByProductId(productId).stream()
                .map(mapping -> mapping.getTopping().getId())
                .collect(Collectors.toList());
    }
}