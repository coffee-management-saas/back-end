package com.futurenbetter.saas.modules.product.service.inter;

import com.futurenbetter.saas.modules.product.dto.filter.ProductFilter;
import com.futurenbetter.saas.modules.product.dto.request.ProductRequest;
import com.futurenbetter.saas.modules.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    ProductResponse update(Long id, ProductRequest request);
    ProductResponse getDetail(Long id);
    Page<ProductResponse> getAll(ProductFilter filter);
    void updateAllowToppings(Long productId, List<Long> toppingIds);
    List<Long> getAllowToppingIds(Long productId);
}