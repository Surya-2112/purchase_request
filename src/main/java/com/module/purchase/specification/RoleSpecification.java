package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;
import com.module.purchase.entity.Role;
import com.module.purchase.enums.EmployeeGroup;

public class RoleSpecification {
    
    public static Specification<Role> hasRoleId(Long roleId)
    {
        return (root,query,cb)->
         roleId == null? null
         : cb.equal(root.get("roleId"),roleId);
    }

    public static Specification<Role> hasRoleName(String roleName)
    {
        return (root,query,cb) ->
        roleName == null || roleName.isEmpty()
        ? null
        : cb.like(cb.lower(root.get("roleName")),roleName.toLowerCase()); 
    }

   public static Specification<Role> hasEmployeeGroup(EmployeeGroup group)
    {
        return (root, query, cb) -> 
        group == null? null
        :cb.equal(root.join("employeeGroups"),group);
    }

}
