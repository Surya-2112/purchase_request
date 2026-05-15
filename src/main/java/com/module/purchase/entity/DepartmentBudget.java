package com.module.purchase.entity;

import java.time.Year;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class DepartmentBudget {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long departmentBudgetId;
    
    @ManyToOne
    @JoinColumn(name = "departmentId")
    private Department department;

    private Double TotalBudgetAmount;

    private Double RemainingBudgetAmount;

    private Year year;

    public Long getDepartmentBudgetId() {
        return departmentBudgetId;
    }

    public void setDepartmentBudgetId(Long departmentBudgetId) {
        this.departmentBudgetId = departmentBudgetId;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Double getTotalBudgetAmount() {
        return TotalBudgetAmount;
    }

    public void setTotalBudgetAmount(Double totalBudgetAmount) {
        TotalBudgetAmount = totalBudgetAmount;
    }

    public Double getRemainingBudgetAmount() {
        return RemainingBudgetAmount;
    }

    public void setRemainingBudgetAmount(Double remainingBudgetAmount) {
        RemainingBudgetAmount = remainingBudgetAmount;
    }

    public Year getYear() {
        return year;
    }

    public void setYear(Year year) {
        this.year = year;
    }

}
