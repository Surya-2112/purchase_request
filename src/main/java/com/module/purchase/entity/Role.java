package com.module.purchase.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.module.purchase.enums.EmployeeGroup;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

@Entity
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    private String roleName;

    @CollectionTable(
        name = "role_employee_groups",
        joinColumns = @JoinColumn(name = "role_id")
    )
    @Column(name = "employee_group")
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER, targetClass = EmployeeGroup.class)
    private List<EmployeeGroup> employeeGroups;

    @OneToMany(mappedBy = "role")
    @JsonIgnoreProperties({"role"})
    private List<Employee> employees;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<EmployeeGroup> getEmployeeGroups() {
        return employeeGroups;
    }

    public void setEmployeeGroups(List<EmployeeGroup> employeeGroups) {
        this.employeeGroups = employeeGroups;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Role{");
        sb.append("roleId=").append(roleId);
        sb.append(", roleName=").append(roleName);
        sb.append('}');
        return sb.toString();
    }


}
