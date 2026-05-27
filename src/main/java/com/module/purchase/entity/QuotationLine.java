package com.module.purchase.entity;

// import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// @Entity
public class QuotationLine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lineId;

   // private Quotation

    public Long getLineId() {
        return lineId;
    }

    public void setLineId(Long lineId) {
        this.lineId = lineId;
    }
}
