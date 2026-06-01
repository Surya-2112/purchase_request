package com.module.purchase.entity;

import java.time.LocalDate;
import com.module.purchase.enums.Status;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class PurchaseOrderHeader {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseOrderId;

    @ManyToOne
    @JoinColumn(name = "employeeId")
    private Employee createdBy;

    @OneToOne
    @JoinColumn(name = "purchaseRequestId")
    private PurchaseRequestHeader purchaseRequestHeader;

    private Double totalAmount;

    @ManyToOne
    @JoinColumn(name = "vendorId")
    private Vendor vendor;

    private LocalDate createdDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Employee getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Employee createdBy) {
        this.createdBy = createdBy;
    }

    public PurchaseRequestHeader getPurchaseRequestHeader() {
        return purchaseRequestHeader;
    }

    public void setPurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader) {
        this.purchaseRequestHeader = purchaseRequestHeader;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
