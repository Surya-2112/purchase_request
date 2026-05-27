package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.module.purchase.entity.PurchaseRequestDocument;
import com.module.purchase.entity.PurchaseRequestHeader;

public interface PurchaseRequestDocumentRepositor extends JpaRepository<PurchaseRequestDocument, Long> {

    List<PurchaseRequestDocument> findAllByPurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader);
}