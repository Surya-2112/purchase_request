package com.module.purchase.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.module.purchase.entity.ViewPermission;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.ViewName;

public interface ViewPermissionRepository extends JpaRepository<ViewPermission, Long> {

    List<ViewPermission> findByViewName(ViewName viewName);

    boolean existsByViewNameAndEmployeeGroup( ViewName viewName,  EmployeeGroup employeeGroup);
}