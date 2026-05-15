package com.module.purchase.specification;

import java.time.Year;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.Department;
import com.module.purchase.entity.DepartmentBudget;

public class DepartmentBudgetSpecification {
    
    public static Specification<DepartmentBudget> hasDeparmentBudgetId(Long departmentBudgetId)
    {
        return (root,query,cb)->
            departmentBudgetId == null? null
            : cb.equal(root.get("departmentBudgetId"),departmentBudgetId);
        
    }

    public static Specification<DepartmentBudget> hasDeparment(Department department)
    {
        return (root,query,cb)->
            department == null? null
            : cb.equal(root.get("department"),department);
        
    }

    public static Specification<DepartmentBudget> hasDeparmentBudgetYear(Year year)
    {
        return (root,query,cb)->
            year == null? null
            : cb.equal(root.get("year"),year);
    }
}
