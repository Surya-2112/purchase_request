package com.module.purchase.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


import com.module.purchase.entity.PurchaseRequestHeader;

public interface PurchaseRequestHeaderRepository extends JpaRepository<PurchaseRequestHeader, Long> ,JpaSpecificationExecutor<PurchaseRequestHeader> {
    
   // Optional<PurchaseRequestHeader> findDetailsById(Long id);
}