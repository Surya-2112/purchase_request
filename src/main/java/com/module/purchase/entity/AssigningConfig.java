package com.module.purchase.entity;

import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AssigningConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ApprovalType approvalType;

    private Integer level;  

    @Enumerated(EnumType.STRING)
    private EmployeeGroup employeeGroup;

    private Double MinAmount;

    private Double MaxAmount;

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
        return MinAmount;
    }

    public void setMinAmount(Double MinAmount) {
        this.MinAmount = MinAmount;
    }

    public Double getMaxAmount() {
        return MaxAmount;
    }

    public void setMaxAmount(Double MaxAmount) {
        this.MaxAmount = MaxAmount;
    }


}
