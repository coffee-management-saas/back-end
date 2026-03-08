package com.futurenbetter.saas.modules.product.service.inter;

import com.futurenbetter.saas.modules.product.dto.filter.ProductVariantFilter;
import com.futurenbetter.saas.modules.product.dto.request.ProductVariantRequest;
import com.futurenbetter.saas.modules.product.dto.response.ProductVariantResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductVariantService {
    ProductVariantResponse create(ProductVariantRequest request);
    ProductVariantResponse update(Long id, ProductVariantRequest request);
    ProductVariantResponse getDetail(Long id);

    List<ProductVariantResponse> getByProductId(Long productId);

    Page<ProductVariantResponse> getAll(ProductVariantFilter filter);
}