package com.module.purchase.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.enums.ApprovalType;


public interface AssigningConfigRepository
        extends JpaRepository<AssigningConfig, Long>, JpaSpecificationExecutor<AssigningConfig> {

        List<AssigningConfig> findByApprovalTypeAndMinAmountLessThanEqual(ApprovalType approvalType,Double Amount);

}
