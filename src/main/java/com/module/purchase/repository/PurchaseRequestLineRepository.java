package com.module.purchase.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.module.purchase.entity.ItemVariant;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.enums.Status;

public interface PurchaseRequestLineRepository extends JpaRepository<PurchaseRequestLine, Long>, JpaSpecificationExecutor<PurchaseRequestLine> {
    
    List<PurchaseRequestLine>  findByPurchaseRequestHeader(PurchaseRequestHeader header);

     List<PurchaseRequestLine> findByRequestForQuotation(RequestForQuotation rfq);

     List<PurchaseRequestLine> findByItemVariantAndRequestForQuotationIsNullAndStatusIn( ItemVariant itemVariant,List<Status> statuses);

}