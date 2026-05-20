package com.module.purchase.repository;

import java.time.Year;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.module.purchase.entity.Department;
import com.module.purchase.entity.DepartmentBudget;
import java.util.List;

public interface DepartmentBudgetRepository extends JpaRepository<DepartmentBudget, Long> ,JpaSpecificationExecutor<DepartmentBudget> {
    
    Optional<DepartmentBudget> findByDepartmentAndYear(Department department, Year year);
    List<DepartmentBudget> findByYear(Year year);
}
