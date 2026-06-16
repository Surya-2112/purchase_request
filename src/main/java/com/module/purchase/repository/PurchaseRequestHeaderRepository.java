package com.module.purchase.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.enums.Status;

public interface PurchaseRequestHeaderRepository extends JpaRepository<PurchaseRequestHeader, Long>, JpaSpecificationExecutor<PurchaseRequestHeader> {

    Long countByStatus(Status status);

    List<PurchaseRequestHeader> findAllByOrderByPurchaseRequestIdDesc(PageRequest pageRequest);

    Long countByCreatedBy(Employee employee);

    Long countByStatusAndCreatedBy(Status status, Employee employee);

    List<PurchaseRequestHeader> findByCreatedByOrderByPurchaseRequestIdDesc(Employee employee, Pageable pageable);
}
