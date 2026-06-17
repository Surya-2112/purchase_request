package com.module.purchase.entity;

import java.time.Year;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table( name = "department_budget")
public class DepartmentBudget {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_budget_id")
    private Long departmentBudgetId;
    
    @NotNull
    @ManyToOne
     @JoinColumn(name = "department_id", referencedColumnName = "department_id")
    private Department department;

    @NotNull
    @Column(name = "total_budget_amount")
    @Positive
    private Double TotalBudgetAmount;

    @NotNull
    @Column(name = "remaining_budget_amount")
    @PositiveOrZero
    private Double RemainingBudgetAmount;

    @NotNull
    @Column(name = "budget_year")
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
