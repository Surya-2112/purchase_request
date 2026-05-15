package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Role;

public class EmployeeSpecification {
    
    public static Specification<Employee> hasEmployeeId(Long id)
    {
        return (root,query,cb)->
        id == null ? null
        : cb.equal(root.get("employeeId"),id);
    }

    public static Specification<Employee> hasEmployeeName(String name)
    {
        return (root,query,cb)->
        name==null || name.isEmpty()
        ? null
        : cb.like(cb.lower(root.get("employeeName")),"%"+name.toLowerCase()+"%");
    }

    public static Specification<Employee> hasActive(Boolean active) 
    {
        return (root,query,cb) ->
        active==null ?null
        :cb.equal(root.get("active"),active);
    }

    public static Specification<Employee> hasDepartment(Department department)
    {
        return (root,query,cb)->
        department == null ? null
        :cb.equal(root.get("department"),department);
    }

    public static Specification<Employee> hasRole(Role role)
    {
        return (root,query,cb)->
        role == null ? null
        :cb.equal(root.get("role"),role); 
    }
}
