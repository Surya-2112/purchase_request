package com.module.purchase.entityDTO;

import java.time.Year;

import com.module.purchase.entity.Department;

public class DepartmentBudgetDTO {
    
    private Long departmentBudgetId;
    
    private Department department;

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

    public Year getYear() {
        return year;
    }

    public void setYear(Year year) {
        this.year = year;
    }

    
}
