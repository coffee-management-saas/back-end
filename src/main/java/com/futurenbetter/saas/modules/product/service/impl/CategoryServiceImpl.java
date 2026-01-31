package com.futurenbetter.saas.modules.product.service.impl;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.product.dto.request.CategoryRequest;
import com.futurenbetter.saas.modules.product.dto.response.CategoryResponse;
import com.futurenbetter.saas.modules.product.entity.Category;
import com.futurenbetter.saas.modules.product.enums.Status;
import com.futurenbetter.saas.modules.product.mapper.CategoryMapper;
import com.futurenbetter.saas.modules.product.repository.CategoryRepository;
import com.futurenbetter.saas.modules.product.service.inter.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ShopRepository shopRepository;

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Long shopId = TenantContext.getCurrentShopId();

        if (categoryRepository.existsByNameAndShopId(request.getName(), shopId)) {
            throw new BusinessException("Tên danh mục đã tồn tại");
        }

        Category category = categoryMapper.toEntity(request);
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException("Cửa hàng không tồn tại"));
        category.setShop(shop);
        category.setStatus(Status.ACTIVE);

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Long shopId = TenantContext.getCurrentShopId();
        Category category = categoryRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new BusinessException("Danh mục không tồn tại"));

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByNameAndShopId(request.getName(), shopId)) {
                throw new BusinessException("Tên danh mục đã tồn tại");
            }
        }

        categoryMapper.updateFromRequest(category, request);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse getDetail(Long id) {
        return categoryRepository.findByIdAndShopId(id, TenantContext.getCurrentShopId())
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Danh mục không tồn tại"));
    }

    @Override
    public Page<CategoryResponse> getAll(BaseFilter filter) {
        Long shopId = TenantContext.getCurrentShopId();
        return categoryRepository.findAllByShopId(shopId, filter.getPageable())
                .map(categoryMapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findByIdAndShopId(id, TenantContext.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Danh mục không tồn tại"));

        category.setStatus(Status.DELETED);
        categoryRepository.save(category);
    }
}