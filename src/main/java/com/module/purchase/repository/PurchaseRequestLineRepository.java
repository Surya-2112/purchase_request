package com.module.purchase.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.enums.Status;

public interface PurchaseRequestLineRepository extends JpaRepository<PurchaseRequestLine, Long>, JpaSpecificationExecutor<PurchaseRequestLine> {
    
    List<PurchaseRequestLine>  findByPurchaseRequestHeader(PurchaseRequestHeader header);

    @Query("SELECT prl FROM PurchaseRequestLine prl WHERE prl.requestForQuotation IS NULL " +
           "AND prl.status <> :draftStatus " +
           "AND prl.approvedQuantity > 0.0")
    List<PurchaseRequestLine> findAvailableApprovedLinesForRfq(@Param("draftStatus") Status draftStatus);

     List<PurchaseRequestLine> findByRequestForQuotation(RequestForQuotation rfq);

}