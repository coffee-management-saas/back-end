package com.futurenbetter.saas.modules.order.specification;

import com.futurenbetter.saas.modules.order.dto.filter.OrderFilter;
import com.futurenbetter.saas.modules.order.entity.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {
    public static Specification<Order> filter(Long shopId, OrderFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("shop").get("id"), shopId));

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("orderStatus"), filter.getStatus()));
            }

            if (filter.getOrderType() != null) {
                predicates.add(cb.equal(root.get("orderType"), filter.getOrderType()));
            }

            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate()));
            }

            if (filter.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), filter.getCustomerId()));
            }

            // (Tùy chọn) Lọc theo nhân viên tạo đơn
            // Nếu Order entity của bạn có @CreatedBy hoặc link tới Employee, hãy thêm vào đây
            // if (filter.getCreatedBy() != null) {
            //     predicates.add(cb.equal(root.get("createdBy"), filter.getCreatedBy()));
            // }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
