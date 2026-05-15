package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.enums.Status;

public class PurchaseRequestSpecification {
    
    public static Specification<PurchaseRequestHeader> hasPurchaseRequestId(Long purchaseRequestId) {
        return (root, query, cb) ->
                purchaseRequestId == null
                        ? null
                        : cb.equal(root.get("purchaseRequestId"), purchaseRequestId);
    }

    public static Specification<PurchaseRequestHeader> hasCreatedBy(Employee createdBy) {
        return (root, query, cb) ->
                createdBy == null
                        ? null
                        : cb.equal(root.get("createdBy"), createdBy);
    }

    public static Specification<PurchaseRequestHeader> hasStatus(Status status) {
        return (root, query, cb) ->
                status == null
                        ? null
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<PurchaseRequestHeader> hasForDepartment(Department forDepartment) {
        return (root, query, cb) ->
                forDepartment == null
                        ? null
                        : cb.equal(root.get("forDepartment"), forDepartment);
    }
}
