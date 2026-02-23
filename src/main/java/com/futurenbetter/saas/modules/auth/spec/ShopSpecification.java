package com.futurenbetter.saas.modules.auth.spec;

import com.futurenbetter.saas.modules.auth.dto.filter.ShopFilter;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ShopSpecification {
    public static Specification<Shop> filter(ShopFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.getKeyword())) {
                String pattern = "%" + filter.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("shopName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("phone")), pattern),
                        cb.like(cb.lower(root.get("domain")), pattern)
                ));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("shopStatus"), filter.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
