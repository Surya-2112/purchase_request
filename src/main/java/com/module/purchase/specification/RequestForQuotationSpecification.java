package com.module.purchase.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.Category;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entity.Vendor;
import com.module.purchase.enums.RequestForQuotationStatus;

public class RequestForQuotationSpecification {

    public static Specification<RequestForQuotation> hasId(Long id) {
        return (root, query, cb) -> {
            if (id == null) {
                return null;
            }
            return cb.equal(root.get("id"), id);
        };
    }

    public static Specification<RequestForQuotation> hasStatus(RequestForQuotationStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<RequestForQuotation> hasRequestedDate(LocalDate requestedDate) {
        return (root, query, cb) -> {
            if (requestedDate == null) {
                return null;
            }
            return cb.equal(root.get("requestedDate"), requestedDate);
        };
    }

    public static Specification<RequestForQuotation> hasCategory(Category category) {
        return (root, query, cb)
                -> category == null ? null
                        : cb.equal(root.get("category"), category);

    }

    public static Specification<RequestForQuotation> belongsToVendorCategories(Vendor vendor) {
        return (root, query, criteriaBuilder) -> 
             vendor == null ? null 
            : root.get("category").in(vendor.getCategories());
    }
}
