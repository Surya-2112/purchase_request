package com.module.purchase.entity;

import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.ViewName;

import jakarta.persistence.*;

@Entity
@Table(name = "view_permission") 
public class ViewPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_permission_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "view_name")
    private ViewName viewName;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_group")
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