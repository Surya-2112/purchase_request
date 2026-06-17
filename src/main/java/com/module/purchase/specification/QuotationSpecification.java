package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.Vendor;
import com.module.purchase.entity.RequestForQuotation; 
import com.module.purchase.enums.Status;
import jakarta.persistence.criteria.Join;

public class QuotationSpecification {
    
    public static Specification<Quotation> hasQuotationId(Long quotationId) {
        return (root, query, cb) ->
                quotationId == null
                        ? null
                        : cb.equal(root.get("id"), quotationId);
    }

    public static Specification<Quotation> hasRequestForQuotationId(Long rfqId) {
        return (root, query, cb) -> {
            if (rfqId == null) return null;
            Join<Quotation, RequestForQuotation> rfqJoin = root.join("requestForQuotation");
            return cb.equal(rfqJoin.get("id"), rfqId);
        };
    }

    public static Specification<Quotation> hasVendor(Vendor vendor) {
        return (root, query, cb) ->
                vendor == null
                        ? null
                        : cb.equal(root.get("vendor"), vendor);
    }

    public static Specification<Quotation> hasSupplierNameLike(String supplierName) {
        return (root, query, cb) -> {
            if (supplierName == null || supplierName.trim().isEmpty()) return null;

            Join<Quotation, Vendor> vendorJoin = root.join("vendor");
            return cb.like(cb.lower(vendorJoin.get("vendorName")), "%" + supplierName.toLowerCase().trim() + "%");
        };
    }

    public static Specification<Quotation> hasStatus(Status status) {
        return (root, query, cb) ->
                status == null
                        ? null
                        : cb.equal(root.get("status"), status);
    }
}