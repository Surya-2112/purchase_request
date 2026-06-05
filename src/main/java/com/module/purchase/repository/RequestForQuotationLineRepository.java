package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.module.purchase.entity.RequestForQuotationLine;

public interface RequestForQuotationLineRepository extends JpaRepository<RequestForQuotationLine, Long> {
    
}
