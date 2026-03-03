package com.futurenbetter.saas.modules.dashboard.service.inter;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.dashboard.dto.filter.MonthlyProductSoldFilter;
import com.futurenbetter.saas.modules.dashboard.dto.response.MonthlyProductSoldResponse;
import com.futurenbetter.saas.modules.product.entity.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MonthlyProductSoldService {
    boolean updateMonthlyProductSold(Shop shop, Product product, Integer quantitySold);
    List<MonthlyProductSoldResponse> getAll(MonthlyProductSoldFilter filter);
}
