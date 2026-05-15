package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.module.purchase.entity.PurchaseRequestHeader;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PurchaseRequestHeaderRepository extends JpaRepository<PurchaseRequestHeader, Long> ,JpaSpecificationExecutor<PurchaseRequestHeader> {
    
}
