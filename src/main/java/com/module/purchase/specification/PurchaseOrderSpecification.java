package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.Vendor;
import com.module.purchase.enums.Status;

public class PurchaseOrderSpecification {

        public static Specification<PurchaseOrderHeader> hasPurchaseOrderId(Long purchaseOrderId) {

                return (root, query, cb) ->
                        purchaseOrderId == null? null
                        : cb.equal( root.get("purchaseOrderId"),purchaseOrderId);
        }

        public static Specification<PurchaseOrderHeader> hasCreatedBy(Employee createdBy) {

                return (root, query, cb) ->
                createdBy == null? null
                : cb.equal(root.get("createdBy"),createdBy);
        }

        public static Specification<PurchaseOrderHeader> hasStatus(Status status) {

                return (root, query, cb) ->

                status == null? null

                                : cb.equal( root.get("status"),
                                                status);
        }

        public static Specification<PurchaseOrderHeader> hasVendor(Vendor vendor) {

                return (root, query, cb) ->
                vendor == null ? null
                                : cb.equal(root.get("vendor"),vendor);
        }
}