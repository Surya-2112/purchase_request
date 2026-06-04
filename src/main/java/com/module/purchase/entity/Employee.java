package com.module.purchase.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    @NotBlank
    @Column(nullable = false, length=72)
    @Size(min=3,max=72)
    private String employeeName;

    @NotBlank
    @Column(unique = true , nullable = false)
    @Email
    private String employeeEmail;

    @Column(length=15)
    @Size(min=10,max=15)
    private String employeePhoneNumber;

    @ManyToOne
    @JoinColumn(name = "departmentId")
    @JsonIgnoreProperties({ "employees", "headEmployee" })
    private Department department;

    @ManyToOne
    @JoinColumn(name = "roleId")
    @JsonIgnoreProperties({ "employees" })
    private Role role;

    @OneToMany(mappedBy = "defaultApprover")
    @JsonIgnoreProperties({ "defaultApprover" })
    private List<AssigningConfig> assigningConfigs;

    @Embedded
    private Address address;

    @NotNull
    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("true")
    private Boolean active = true;

    @OneToOne
    @JoinColumn(name = "userId")
    @JsonIgnoreProperties({ "employee" })
    private Users user;

    @OneToMany(mappedBy = "createdBy")
    @JsonIgnoreProperties({ "createdBy" })
    private List<PurchaseRequestHeader> purchaseRequestHeaders;

    @OneToMany(mappedBy = "createdBy")
    @JsonIgnoreProperties({ "createdBy" })
    private List<PurchaseOrderHeader> purchaseOrderHeaders;

    @OneToMany(mappedBy = "approver")
    @JsonIgnoreProperties({ "approver" })
    private List<AssigningApprovals> forApprovals;

    @OneToMany(mappedBy = "assignedBy")
    @JsonIgnoreProperties({ "assignedBy" })
    private List<AssigningApprovals> assignedApprovals;

    @OneToMany(mappedBy = "performedBy")
    @JsonIgnoreProperties({ "performedBy" })
    private List<AuditLogs> auditLogs;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getEmployeePhoneNumber() {
        return employeePhoneNumber;
    }

    public void setEmployeePhoneNumber(String employeePhoneNumber) {
        this.employeePhoneNumber = employeePhoneNumber;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public List<PurchaseRequestHeader> getPurchaseRequestHeaders() {
        return purchaseRequestHeaders;
    }

    public void setPurchaseRequestHeaders(List<PurchaseRequestHeader> purchaseRequestHeaders) {
        this.purchaseRequestHeaders = purchaseRequestHeaders;
    }

    public List<AssigningApprovals> getForApprovals() {
        return forApprovals;
    }

    public void setForApprovals(List<AssigningApprovals> forApprovals) {
        this.forApprovals = forApprovals;
    }

    public List<AssigningApprovals> getAssignedApprovals() {
        return assignedApprovals;
    }

    public void setAssignedApprovals(List<AssigningApprovals> assignedApprovals) {
        this.assignedApprovals = assignedApprovals;
    }

    public List<AuditLogs> getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(List<AuditLogs> auditLogs) {
        this.auditLogs = auditLogs;
    }

    public List<PurchaseOrderHeader> getPurchaseOrderHeaders() {
        return purchaseOrderHeaders;
    }

    public void setPurchaseOrderHeaders(List<PurchaseOrderHeader> purchaseOrderHeaders) {
        this.purchaseOrderHeaders = purchaseOrderHeaders;
    }

    @Override
    public String toString() {
        return "Employee [employeeId=" + employeeId + ", employeeName=" + employeeName + ", employeeEmail="
                + employeeEmail + ", employeePhoneNumber=" + employeePhoneNumber + ", address=" + address + ", active="
                + active + "]";
    }

    public List<AssigningConfig> getAssigningConfig() {
        return assigningConfigs;
    }

    public void setAssigningConfig(List<AssigningConfig> assigningConfigs) {
        this.assigningConfigs = assigningConfigs;
    }

}
