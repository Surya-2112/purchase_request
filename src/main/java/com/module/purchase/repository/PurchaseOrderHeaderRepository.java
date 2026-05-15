package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.module.purchase.entity.PurchaseOrderHeader;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PurchaseOrderHeaderRepository extends JpaRepository<PurchaseOrderHeader, Long> ,JpaSpecificationExecutor<PurchaseOrderHeader> {
    
}
