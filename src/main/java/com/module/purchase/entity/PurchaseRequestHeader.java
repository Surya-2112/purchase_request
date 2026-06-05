package com.module.purchase.entity;

import java.sql.Date;
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
import jakarta.validation.constraints.NotNull;

@Entity
public class PurchaseRequestHeader {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseRequestId;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    @JsonIgnoreProperties({"purchaseRequests"})
    private Employee createdBy;

    @NotNull
    @Column( nullable = false)
    private Double totalAmount;
    
    @NotNull
    @Column(nullable = false)
    private Date createdDate;

    @NotNull
    @Column(nullable = false)
    private Integer level;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @OneToMany(mappedBy = "purchaseRequestHeader")
    private List<PurchaseRequestDocument> documents;

    @ManyToOne
    @JoinColumn(name = "department",nullable = false)
    @JsonIgnoreProperties("purchaseRequestHeaders")
    private Department forDepartment;

    @OneToMany(mappedBy = "purchaseRequestHeader")
    @JsonIgnoreProperties({"purchaseRequestHeader"})
    private List<PurchaseRequestLine> purchaseRequestLines;

    @OneToMany(mappedBy = "referenceId")
    @JsonIgnoreProperties({"approver", "assignedBy"})
    private List<AssigningApprovals> assigningApprovals;

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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
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

    public List<AssigningApprovals> getAssigningApprovals() {
        return assigningApprovals;
    }

    public void setAssigningApprovals(List<AssigningApprovals> assigningApprovals) {
        this.assigningApprovals = assigningApprovals;
    }

    public List<PurchaseRequestDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<PurchaseRequestDocument> documents) {
        this.documents = documents;
    }
}
