package com.module.purchase.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.module.purchase.entity.Employee;

public interface  EmployeeRepository extends JpaRepository<Employee, Long>,JpaSpecificationExecutor<Employee> {
    
    Optional<Employee> findByEmployeeEmail(String email);
}
