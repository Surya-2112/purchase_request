package com.module.purchase.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entity.RequestForQuotationLine;

public interface RequestForQuotationLineRepository extends JpaRepository<RequestForQuotationLine, Long> {
    
    List<RequestForQuotationLine> findByRequestForQuotation(RequestForQuotation requestForQuotation);
}
