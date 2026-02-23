package com.futurenbetter.saas.modules.auth.service.Impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.modules.auth.dto.filter.ShopFilter;
import com.futurenbetter.saas.modules.auth.dto.request.ShopRequest;
import com.futurenbetter.saas.modules.auth.dto.response.ShopResponse;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.enums.ShopStatus;
import com.futurenbetter.saas.modules.auth.mapper.ShopMapper;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.auth.service.ShopService;
import com.futurenbetter.saas.modules.auth.spec.ShopSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final ShopMapper shopMapper;

    @Override
    public Page<ShopResponse> getShops(ShopFilter filter) {

        Page<Shop> page = shopRepository.findAll(ShopSpecification.filter(filter), filter.getPageable());
        return page.map(shopMapper::toResponse);
    }

    @Override
    public ShopResponse getShopById(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy cửa hàng"));
        return shopMapper.toResponse(shop);
    }

    @Override
    @Transactional
    public ShopResponse updateShop(Long id, ShopRequest request) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy cửa hàng"));


        shopMapper.updateShopFromRequest(request, shop);
        shop.setUpdatedAt(LocalDateTime.now());

        return shopMapper.toResponse(shopRepository.save(shop));
    }

    @Override
    public ShopResponse createShop(ShopRequest request) {
        Shop shop = shopMapper.toEntity(request);
        shop.setShopStatus(ShopStatus.ACTIVE);
        Shop result = shopRepository.save(shop);
        return shopMapper.toResponse(result);
    }

    @Override
    public ShopResponse deleteShop(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy cửa hàng"));
        shop.setShopStatus(ShopStatus.DELETED);
        shopRepository.save(shop);
        return shopMapper.toResponse(shop);
    }
}