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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"departmentId", "year"}))
public class DepartmentBudget {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long departmentBudgetId;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "departmentId",nullable = false)
    private Department department;

    @NotNull
    @Column(nullable=false)
    @Positive
    private Double TotalBudgetAmount;

    @NotNull
    @Column(nullable=false)
    @PositiveOrZero
    private Double RemainingBudgetAmount;

    @NotNull
    @Column(nullable=false)
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
