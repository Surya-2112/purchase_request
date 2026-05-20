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

@Entity
public class PurchaseRequestHeader {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseRequestId;

    @ManyToOne
    @JoinColumn(name = "employeeId")
    @JsonIgnoreProperties({"purchaseRequests"})
    private Employee createdBy;

    private Double totalAmount;

    private Date createdDate;

    private Integer level;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "department")
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

}
