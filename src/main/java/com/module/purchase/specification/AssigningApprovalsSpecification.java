package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.Employee;
import com.module.purchase.enums.ApprovalType;

public class AssigningApprovalsSpecification {

    public static Specification<AssigningApprovals> hasAssigningApprovalsId(Long assigningApprovalsId) {
        return (root, query, cb) ->
                assigningApprovalsId == null
                        ? null
                        : cb.equal(root.get("assigningApprovalsId"), assigningApprovalsId);
    }

    public static Specification<AssigningApprovals> hasApprover(Employee approver) {
        return (root, query, cb) ->
                approver == null
                        ? null
                        : cb.equal(root.get("approver"), approver);
    }

    public static Specification<AssigningApprovals> hasApprovalType(ApprovalType approvalType) {
        return (root, query, cb) ->
                approvalType == null
                        ? null
                        : cb.equal(root.get("approvalType"), approvalType);
    }

    public static Specification<AssigningApprovals> hasReferenceId(Long referenceId) {
        return (root, query, cb) ->
                referenceId == null
                        ? null
                        : cb.equal(root.get("referenceId"), referenceId);
    }
}