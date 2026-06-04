package com.module.purchase.entity;

import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"approval_type","employee_group"}),
        @UniqueConstraint(columnNames = {"level","approval_type"})
    }
)
public class AssigningConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_amount", nullable = false)
    private Double minAmount;

    @Column(name = "max_amount")
    private Double maxAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_group", nullable = false)
    private EmployeeGroup employeeGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_type", nullable = false)
    private ApprovalType approvalType;

    @Column(name = "margin_difference_percentage")
    private Double marginDifferencePercentage;

    @Column(name = "level")
    private Integer level;

    @ManyToOne
    @JoinColumn(name = "defaultApprover")
    private Employee defaultApprover;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApprovalType getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(ApprovalType approvalType) {
        this.approvalType = approvalType;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public EmployeeGroup getEmployeeGroup() {
        return employeeGroup;
    }

    public void setEmployeeGroup(EmployeeGroup employeeGroup) {
        this.employeeGroup = employeeGroup;
    }

    public Double getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Double minAmount) {
        this.minAmount = minAmount;
    }

    public Double getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Double maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Employee getDefaultApprover() {
        return defaultApprover;
    }

    public void setDefaultApprover(Employee defaultApprover) {
        this.defaultApprover = defaultApprover;
    }

}
