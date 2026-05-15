package com.module.purchase.entity;

import java.sql.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.module.purchase.enums.Status;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

    private Date createdDate;

    private Date expectedDeliveryDate;

    private Integer level;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "referenceId")
    @JsonIgnoreProperties({"approver", "assignedBy"})
    private List<AssigningApprovals> assigningApprovals;

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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(Date expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<AssigningApprovals> getAssigningApprovals() {
        return assigningApprovals;
    }

    public void setAssigningApprovals(List<AssigningApprovals> assigningApprovals) {
        this.assigningApprovals = assigningApprovals;
    }


}
