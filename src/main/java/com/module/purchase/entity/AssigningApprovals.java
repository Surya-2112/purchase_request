package com.module.purchase.entity;

//import java.sql.Date;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.module.purchase.enums.ApprovalSource;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.Status;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class AssigningApprovals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assigningApprovalsId;

    @ManyToOne
    @JoinColumn(name = "approverId")
    @JsonIgnoreProperties({ "department","role","user"})
    private Employee approver;

    @ManyToOne
    @JoinColumn(name = "assignedById")
    @JsonIgnoreProperties({ "department","role","user"})
    private Employee assignedBy;

    private Integer level;

    @Enumerated(EnumType.STRING)
    private ApprovalType approvalType;

    private Long referenceId;

    private LocalDate assignedDate;

    private LocalDate ApprovedDate;

    private String comments;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private ApprovalSource source;

    public ApprovalSource getSource() {
        return source;
    }

    public void setSource(ApprovalSource source) {
        this.source = source;
    }

    public Long getAssigningApprovalsId() {
        return assigningApprovalsId;
    }

    public void setAssigningApprovalsId(Long assigningApprovalsId) {
        this.assigningApprovalsId = assigningApprovalsId;
    }

    public Employee getApprover() {
        return approver;
    }

    public void setApprover(Employee approver) {
        this.approver = approver;
    }

    public Employee getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Employee assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public ApprovalType getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(ApprovalType approvalType) {
        this.approvalType = approvalType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDate getApprovedDate() {
        return ApprovedDate;
    }

    public void setApprovedDate(LocalDate approvedDate) {
        ApprovedDate = approvedDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
