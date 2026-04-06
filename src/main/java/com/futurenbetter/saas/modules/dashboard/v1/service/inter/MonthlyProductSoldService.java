package com.futurenbetter.saas.modules.dashboard.v1.service.inter;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.dashboard.v1.dto.filter.MonthlyProductSoldFilter;
import com.futurenbetter.saas.modules.dashboard.v1.dto.response.MonthlyProductSoldResponse;
import com.futurenbetter.saas.modules.product.entity.Product;

import java.util.List;

public interface MonthlyProductSoldService {
    boolean updateMonthlyProductSold(Shop shop, Product product, Integer quantitySold);
    List<MonthlyProductSoldResponse> getAll(MonthlyProductSoldFilter filter);
}
