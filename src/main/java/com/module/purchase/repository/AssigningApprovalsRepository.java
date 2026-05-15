package com.module.purchase.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.Employee;
import com.module.purchase.enums.ApprovalType;

public interface AssigningApprovalsRepository extends JpaRepository<AssigningApprovals, Long>,JpaSpecificationExecutor<AssigningApprovals> {

    List<AssigningApprovals> findByApproverAndApprovalType(Employee approver,ApprovalType approvalType);
}
