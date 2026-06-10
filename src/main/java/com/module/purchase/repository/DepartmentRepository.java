package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.module.purchase.entity.Department;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long>,JpaSpecificationExecutor<Department> {
    
    Optional<Department> findByDepartmentCode(String departmentCode);
    Optional<Department> findByDepartmentName(String departmentName);
}
