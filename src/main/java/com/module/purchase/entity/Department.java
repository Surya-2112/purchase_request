package com.module.purchase.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long departmentId;

    private String departmentName;

    private String departmentCode;

    @OneToOne
    @JoinColumn(name = "headEmployeeId")
    @JsonIgnoreProperties({
    "department",
    "role",
    "user",
    "address"
})
    private Employee headEmployee;
   
    private Boolean active;

    @OneToMany(mappedBy = "department")
    @JsonIgnoreProperties({"department"})
    private List<Employee> employees;

    @OneToMany(mappedBy = "department")
    @JsonIgnoreProperties({"department"})
    private List<DepartmentBudget> departmentBudgets;

    @OneToMany(mappedBy = "forDepartment")
    @JsonIgnoreProperties({"forDepartment"})
    private List<PurchaseRequestHeader> purchaseRequestHeaders;

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public Employee getHeadEmployee() {
        return headEmployee;
    }

    public void setHeadEmployee(Employee headEmployee) {
        this.headEmployee = headEmployee;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }
    public List<DepartmentBudget> getDepartmentBudgets() {
        return departmentBudgets;
    }

    public void setDepartmentBudgets(List<DepartmentBudget> departmentBudgets) {
        this.departmentBudgets = departmentBudgets;
    }

    public List<PurchaseRequestHeader> getPurchaseRequestHeaders() {
        return purchaseRequestHeaders;
    }

    public void setPurchaseRequestHeaders(List<PurchaseRequestHeader> purchaseRequestHeaders) {
        this.purchaseRequestHeaders = purchaseRequestHeaders;
    }

    @Override
    public String toString() {
        return "Department [departmentId=" + departmentId + ", departmentName=" + departmentName + ", departmentCode="
                + departmentCode + ", headEmployee=" + headEmployee + ", active=" + active + "]";
    }

    

}

