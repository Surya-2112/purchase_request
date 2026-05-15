package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.module.purchase.entity.PurchaseOrderLine;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, Long>,JpaSpecificationExecutor<PurchaseOrderLine> {
    
}
