package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.module.purchase.entity.Quotation;

public interface QuotationRepository extends JpaRepository<Quotation, Long>{
    
}
