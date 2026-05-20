package com.module.purchase.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.enums.Status;

public interface PurchaseRequestHeaderRepository extends JpaRepository<PurchaseRequestHeader, Long> ,JpaSpecificationExecutor<PurchaseRequestHeader> {
    
    Long countByStatus(Status status);
    
    List<PurchaseRequestHeader> findAllByOrderByPurchaseRequestIdDesc(PageRequest pageRequest);
}