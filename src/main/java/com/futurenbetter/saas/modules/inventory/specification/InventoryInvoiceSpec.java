package com.futurenbetter.saas.modules.inventory.specification;

import com.futurenbetter.saas.modules.inventory.dto.filter.InventoryInvoiceFilter;
import com.futurenbetter.saas.modules.inventory.entity.InventoryInvoice;
import com.futurenbetter.saas.modules.inventory.entity.InventoryInvoiceDetail;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class InventoryInvoiceSpec {

    public static Specification<InventoryInvoice> filter(InventoryInvoiceFilter filter, Long shopId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<InventoryInvoice, InventoryInvoiceDetail> detailJoin = root.join("details", JoinType.LEFT);

            predicates.add(cb.equal(root.get("shop").get("id"), shopId));

            if (StringUtils.hasText(filter.getSearch())) {
                String keyword = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate codePred = cb.like(cb.lower(root.get("code")), keyword);
                Predicate supplierPred = cb.like(cb.lower(detailJoin.get("supplierName")), keyword);

                predicates.add(cb.or(codePred, supplierPred));
            }

            // 3. Date Range (Imported At)
            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("importedAt"), filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("importedAt"), filter.getToDate()));
            }

            // 4. Created By
            if (filter.getCreatedBy() != null) {
                predicates.add(cb.equal(root.get("createdBy"), filter.getCreatedBy()));
            }

            // 5. Status
            if (filter.getInventoryStatus() != null) {
                predicates.add(cb.equal(root.get("inventoryStatus"), filter.getInventoryStatus()));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}