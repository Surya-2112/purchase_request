package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;

public class AssigningConfigSpecification {

    public static Specification<AssigningConfig> hasId(Long id) {
        return (root, query, cb) ->
                id == null ? null
                : cb.equal(root.get("id"), id);
    }

    public static Specification<AssigningConfig> hasApprovalType(ApprovalType approvalType) {
        return (root, query, cb) ->
                approvalType == null ? null
                : cb.equal(root.get("approvalType"), approvalType);
    }

    public static Specification<AssigningConfig> hasLevel(Integer level) {
        return (root, query, cb) ->
                level == null ? null
                : cb.equal(root.get("level"), level);
    }

    public static Specification<AssigningConfig> hasEmployeeGroup(EmployeeGroup employeeGroup) {
        return (root, query, cb) ->
                employeeGroup == null ? null
                : cb.equal(root.get("employeeGroup"), employeeGroup);
    }
}