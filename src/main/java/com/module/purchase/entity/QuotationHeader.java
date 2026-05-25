package com.module.purchase.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

import com.module.purchase.enums.Status;

// @Entity
public class QuotationHeader {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quotationId;

    private PurchaseRequestHeader prheader;

    private LocalDate quotationDate;

    private Vendor vendor;

    private Double totalAmount;

    private Status status;

}
