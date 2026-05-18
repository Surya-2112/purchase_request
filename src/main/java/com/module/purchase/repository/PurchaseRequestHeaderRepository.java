package com.module.purchase.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.module.purchase.entity.PurchaseRequestHeader;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseRequestHeaderRepository extends JpaRepository<PurchaseRequestHeader, Long> ,JpaSpecificationExecutor<PurchaseRequestHeader> {
    
    @Query("""
            SELECT DISTINCT pr
            FROM PurchaseRequestHeader pr
            LEFT JOIN FETCH pr.purchaseRequestLines
            LEFT JOIN FETCH pr.assigningApprovals
            WHERE pr.purchaseRequestId = :id
            """)
    Optional<PurchaseRequestHeader> findDetailsById(Long id);
}
