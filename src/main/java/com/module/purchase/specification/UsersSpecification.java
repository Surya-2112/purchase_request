package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Users;

public class UsersSpecification {
    
    public static Specification<Users> hasUserId(Long userId)
    {
        return (root,query,cb)->
         userId == null? null
         : cb.equal(root.get("userId"),userId);
    }

    public static Specification<Users> hasUserName(String userName)
    {
        return (root,query,cb) ->
        userName == null || userName.isEmpty() ? null
        :cb.like(cb.lower(root.get("userName")),"%"+userName.toLowerCase()+"%");
    }

    public static Specification<Users> hasuserEmail(String userEmail)
    {
        return (root,query,cb) ->
        userEmail == null || userEmail.isEmpty() ? null
        : cb.like(cb.lower(root.get("userEmail")),"%"+userEmail.toLowerCase()+"%");
    }
    
    public static Specification<Users> hasEmployee(Employee employee)
    {
        return (root,query,cb)->
         employee == null? null
         : cb.equal(root.get("employee"),employee);
    }

    public static Specification<Users> hasActive(Boolean active)
    {
        return (root,query,cb) ->
        active == null ? null
        :cb.equal(root.get("active"),active);
    }
}
