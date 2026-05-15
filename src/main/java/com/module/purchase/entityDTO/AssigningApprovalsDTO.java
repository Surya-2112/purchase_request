package com.module.purchase.entityDTO;

import com.module.purchase.entity.Employee;
import com.module.purchase.enums.ApprovalType;

public class AssigningApprovalsDTO {
    
     private Long assigningApprovalsId;

    private Employee approver;

    private ApprovalType approvalType;

    private Long referenceId;

    public Long getAssigningApprovalsId() {
        return assigningApprovalsId;
    }

    public void setAssigningApprovalsId(Long assigningApprovalsId) {
        this.assigningApprovalsId = assigningApprovalsId;
    }

    public Employee getApprover() {
        return approver;
    }

    public void setApprover(Employee approver) {
        this.approver = approver;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public ApprovalType getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(ApprovalType approvalType) {
        this.approvalType = approvalType;
    }

}
