package com.module.purchase.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import com.module.purchase.entity.PurchaseOrderHeader;


import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.module.purchase.enums.Status;
import java.util.List;

public interface PurchaseOrderHeaderRepository extends JpaRepository<PurchaseOrderHeader, Long> ,JpaSpecificationExecutor<PurchaseOrderHeader> {

    Long countByStatus(Status status);

    List<PurchaseOrderHeader> findAllByOrderByPurchaseOrderIdDesc(PageRequest pageRequest);

}
