package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.module.purchase.entity.RequestForQuotation;

public interface RequestForQuotationRepository extends JpaRepository<RequestForQuotation, Long> {
    
}
