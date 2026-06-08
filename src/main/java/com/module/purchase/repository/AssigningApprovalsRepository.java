package com.module.purchase.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.Employee;
import com.module.purchase.enums.ApprovalType;

public interface AssigningApprovalsRepository extends JpaRepository<AssigningApprovals, Long>,JpaSpecificationExecutor<AssigningApprovals> {

    List<AssigningApprovals> findByApproverAndApprovalType(Employee approver,ApprovalType approvalType);
    
    List<AssigningApprovals> findByApprovalTypeAndReferenceId(ApprovalType approvalType,Long referenceId);

    Optional<AssigningApprovals> findByApprovalTypeAndReferenceIdAndLevel(ApprovalType approvalType,Long referenceId,Integer level);

    
}
