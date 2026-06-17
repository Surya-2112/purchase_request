package com.module.purchase.entity;

import java.time.LocalDate;

import com.module.purchase.enums.ApprovalSource;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
    name = "assigning_approvals"
)
public class AssigningApprovals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assigningApprovalsId;

    @ManyToOne
    @JoinColumn(name = "approver_id")
    private Employee approver;

    @ManyToOne
    @JoinColumn(name = "assigned_by_id") 
    private Employee assignedBy;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name="employee_group")
    private EmployeeGroup employeeGroup;

    @NotNull
    @Column(name="approval_level")
    private Integer level;

    @Enumerated(EnumType.STRING)
     @NotNull
    @Column(name="approval_type")
    private ApprovalType approvalType;

    @NotNull
    @Column(name="reference_id")
    private Long referenceId;

    @NotNull
    private LocalDate assignedDate;

    private LocalDate ApprovedDate;

    @Size(max=500)
    private String comments;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Status status;

    @Enumerated(EnumType.STRING)
    @NotNull
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

    public EmployeeGroup getEmployeeGroup() {
        return employeeGroup;
    }

    public void setEmployeeGroup(EmployeeGroup employeeGroup) {
        this.employeeGroup = employeeGroup;
    }

    
}
