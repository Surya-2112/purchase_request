package com.module.purchase.entity;

import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.ViewName;

import jakarta.persistence.*;

@Entity
public class ViewPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ViewName viewName;

    @Enumerated(EnumType.STRING)
    private EmployeeGroup employeeGroup;

    public Long getId() {
        return id;
    }

    public ViewName getViewName() {
        return viewName;
    }

    public void setViewName(ViewName viewName) {
        this.viewName = viewName;
    }

    public EmployeeGroup getEmployeeGroup() {
        return employeeGroup;
    }

    public void setEmployeeGroup(
            EmployeeGroup employeeGroup) {

        this.employeeGroup = employeeGroup;
    }
}