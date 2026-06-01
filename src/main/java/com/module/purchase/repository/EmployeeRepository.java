package com.module.purchase.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.module.purchase.entity.Employee;
import com.module.purchase.enums.EmployeeGroup;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmployeeEmail(String email);

        @Query("""
                select e
                from Employee e
                join e.role r
                join r.employeeGroups g
                where g = :employeeGroup
                """)
        List<Employee> findByRoleEmployeeGroup(@Param("employeeGroup") EmployeeGroup employeeGroup);
    
}
