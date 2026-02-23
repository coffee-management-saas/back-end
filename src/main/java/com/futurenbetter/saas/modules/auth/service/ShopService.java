package com.futurenbetter.saas.modules.auth.service;

import com.futurenbetter.saas.modules.auth.dto.filter.ShopFilter;
import com.futurenbetter.saas.modules.auth.dto.request.ShopRequest;
import com.futurenbetter.saas.modules.auth.dto.response.ShopResponse;
import org.springframework.data.domain.Page;

public interface ShopService {
    Page<ShopResponse> getShops(ShopFilter filter);
    ShopResponse getShopById(Long id);
    ShopResponse updateShop(Long id, ShopRequest request);
    ShopResponse createShop(ShopRequest request);
    ShopResponse deleteShop(Long id);
}
