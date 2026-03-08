package com.futurenbetter.saas.modules.product.specification;

import com.futurenbetter.saas.modules.product.dto.filter.ProductFilter;
import com.futurenbetter.saas.modules.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductSpec {
    public static Specification<Product> filter(ProductFilter filter, Long shopId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Luôn filter theo Shop ID (Bảo mật SaaS)
            predicates.add(cb.equal(root.get("shop").get("id"), shopId));

            // 2. Filter theo Tên (Like)
            if (StringUtils.hasText(filter.getName())) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
            }

            // 3. Filter theo Category
            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            // 4. Filter theo Status
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}