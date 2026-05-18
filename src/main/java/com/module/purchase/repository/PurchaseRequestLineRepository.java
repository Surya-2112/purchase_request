package com.module.purchase.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PurchaseRequestLineRepository extends JpaRepository<PurchaseRequestLine, Long>, JpaSpecificationExecutor<PurchaseRequestLine> {
    
    List<PurchaseRequestLine>  findByPurchaseRequestHeader(PurchaseRequestHeader header);
}
