package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.Department;

public class DepartmentSpecification {

    public static Specification<Department> hasDepartmentId(Long departmentId)
    {
        return (root,query,cb)->
        departmentId == null ? null
        : cb.equal(root.get("departmentId"),departmentId);
    }

    public static Specification<Department> hasDepartmentName(String departmentName)
    {
        return (root,query,cb)->
        departmentName == null || departmentName.isEmpty() 
        ? null
        : cb.like(cb.lower(root.get("departmentName")),"%" + departmentName.toLowerCase() + "%"); 
    }

    public static Specification<Department> hasDepartmentCode(String departmentCode)
    {
        return (root,query,cb) ->
        departmentCode == null || departmentCode.isEmpty()
        ? null
        : cb.like(cb.lower(root.get("departmentCode")),"%" +departmentCode.toLowerCase()+ "%");
    }

    public static Specification<Department> hasActive(Boolean active)
    {
        return (root,query,cb) ->
        active == null ? null
        : cb.equal(root.get("active"),active);
    }
}
