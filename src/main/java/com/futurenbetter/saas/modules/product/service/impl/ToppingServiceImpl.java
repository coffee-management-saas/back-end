package com.futurenbetter.saas.modules.product.service.impl;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.product.dto.request.ToppingRequest;
import com.futurenbetter.saas.modules.product.dto.response.ToppingResponse;
import com.futurenbetter.saas.modules.product.entity.Topping;
import com.futurenbetter.saas.modules.product.enums.Status;
import com.futurenbetter.saas.modules.product.mapper.ToppingMapper;
import com.futurenbetter.saas.modules.product.repository.ToppingRepository;
import com.futurenbetter.saas.modules.product.service.inter.ToppingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ToppingServiceImpl implements ToppingService {

    private final ToppingRepository toppingRepository;
    private final ToppingMapper toppingMapper;
    private final ShopRepository shopRepository;

    @Override
    @Transactional
    public ToppingResponse create(ToppingRequest request) {
        long shopId = TenantContext.getCurrentShopId();
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException("Cửa hàng không tồn tại"));
        Topping topping = toppingMapper.toEntity(request);
        topping.setShop(shop);
        topping.setStatus(Status.ACTIVE);
        return toppingMapper.toResponse(toppingRepository.save(topping));
    }

    @Override
    @Transactional
    public ToppingResponse update(Long id, ToppingRequest request) {
        Topping topping = toppingRepository.findByIdAndShopId(id, TenantContext.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Topping không tồn tại"));

        toppingMapper.updateFromRequest(topping, request);
        return toppingMapper.toResponse(toppingRepository.save(topping));
    }

    @Override
    public ToppingResponse getDetail(Long id) {
        return toppingRepository.findByIdAndShopId(id, TenantContext.getCurrentShopId())
                .map(toppingMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Topping không tồn tại"));
    }

    @Override
    public Page<ToppingResponse> getAll(BaseFilter filter) {
        return toppingRepository.findAllByShopId(TenantContext.getCurrentShopId(), filter.getPageable()).map(toppingMapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Topping topping = toppingRepository.findByIdAndShopId(id, TenantContext.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Topping không tồn tại"));
        topping.setStatus(Status.DELETED);
        toppingRepository.save(topping);
    }
}