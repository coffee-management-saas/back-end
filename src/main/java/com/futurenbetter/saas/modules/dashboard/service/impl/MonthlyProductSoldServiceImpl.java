package com.futurenbetter.saas.modules.dashboard.service.impl;

import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.dashboard.dto.filter.MonthlyProductSoldFilter;
import com.futurenbetter.saas.modules.dashboard.dto.response.MonthlyProductSoldResponse;
import com.futurenbetter.saas.modules.dashboard.entity.MonthlyProductSold;
import com.futurenbetter.saas.modules.dashboard.mapper.MonthlyProductSoldMapper;
import com.futurenbetter.saas.modules.dashboard.repository.MonthlyProductSoldRepository;
import com.futurenbetter.saas.modules.dashboard.service.inter.MonthlyProductSoldService;
import com.futurenbetter.saas.modules.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyProductSoldServiceImpl implements MonthlyProductSoldService {

    private final MonthlyProductSoldRepository monthlyProductSoldRepository;
    private final MonthlyProductSoldMapper monthlyProductSoldMapper;

    @Override
    @Transactional
    public boolean updateMonthlyProductSold(Shop shop, Product product, Integer quantitySold) {

        YearMonth currentMonth = YearMonth.now();
        MonthlyProductSold existingRecord = monthlyProductSoldRepository
                .findByShopIdAndProductIdAndYearMonth(shop.getId(), product.getId(), currentMonth)
                .orElse(null);

        if(existingRecord != null) {
            existingRecord.setQuantitySold(existingRecord.getQuantitySold() + quantitySold);
        } else {
            var newRecord = MonthlyProductSold.builder()
                    .shop(shop)
                    .product(product)
                    .yearMonth(currentMonth)
                    .quantitySold(quantitySold)
                    .build();
            monthlyProductSoldRepository.save(newRecord);
        }

        return true;
    }

    @Override
    public List<MonthlyProductSoldResponse> getAll(MonthlyProductSoldFilter filter) {

        Long shopId = SecurityUtils.getCurrentShopId();

        return monthlyProductSoldRepository.findTopProductsPerMonthByYear(shopId, filter.getYear(), filter.getProductNum())
                .stream()
                .map(monthlyProductSoldMapper::toResponse)
                .toList();
    }
}
