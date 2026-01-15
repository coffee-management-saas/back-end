package com.futurenbetter.saas.modules.product.specification;

import com.futurenbetter.saas.modules.product.dto.filter.ProductVariantFilter;
import com.futurenbetter.saas.modules.product.entity.ProductVariant;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductVariantSpec {
    public static Specification<ProductVariant> filter(ProductVariantFilter filter, Long shopId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("shop").get("id"), shopId));

            // Filter theo Product cha
            if (filter.getProductId() != null) {
                predicates.add(cb.equal(root.get("product").get("id"), filter.getProductId()));
            }

            // Filter theo Size
            if (filter.getSizeId() != null) {
                predicates.add(cb.equal(root.get("size").get("id"), filter.getSizeId()));
            }

            // Filter SKU
            if (StringUtils.hasText(filter.getSkuCode())) {
                predicates.add(cb.like(cb.lower(root.get("skuCode")), "%" + filter.getSkuCode().toLowerCase() + "%"));
            }

            // Filter khoảng giá (Min - Max)
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}