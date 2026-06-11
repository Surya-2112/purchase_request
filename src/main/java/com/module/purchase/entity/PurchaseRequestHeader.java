package com.module.purchase.entity;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.module.purchase.enums.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "purchase_request_header")
public class PurchaseRequestHeader {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "purchase_request_id")
    private Long purchaseRequestId;

    @ManyToOne
     @JoinColumn(name = "requester_id", referencedColumnName = "employee_id")
    @JsonIgnoreProperties({"purchaseRequests"})
    private Employee createdBy;

    @NotNull
    @Column( nullable = false)
    private Double totalAmount;
    
    @NotNull
    @Column(nullable = false)
    private LocalDate createdDate;

   @NotNull
   @Column(name = "approval_level", nullable = false)
    private Integer level;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @OneToMany(mappedBy = "purchaseRequestHeader")
    private List<PurchaseRequestDocument> documents;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    @JsonIgnoreProperties("purchaseRequestHeaders")
    private Department forDepartment;

    @OneToMany(mappedBy = "purchaseRequestHeader")
    @JsonIgnoreProperties({"purchaseRequestHeader"})
    private List<PurchaseRequestLine> purchaseRequestLines;

    public Long getPurchaseRequestId() {
        return purchaseRequestId;
    }

    public void setPurchaseRequestId(Long purchaseRequestId) {
        this.purchaseRequestId = purchaseRequestId;
    }

    public Employee getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Employee createdBy) {
        this.createdBy = createdBy;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
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

    public Department getForDepartment() {
        return forDepartment;
    }

    public void setForDepartment(Department forDepartment) {
        this.forDepartment = forDepartment;
    }

    public List<PurchaseRequestLine> getPurchaseRequestLines() {
        return purchaseRequestLines;
    }

    public void setPurchaseRequestLines(List<PurchaseRequestLine> purchaseRequestLines) {
        this.purchaseRequestLines = purchaseRequestLines;
    }

    public List<PurchaseRequestDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<PurchaseRequestDocument> documents) {
        this.documents = documents;
    }
}
