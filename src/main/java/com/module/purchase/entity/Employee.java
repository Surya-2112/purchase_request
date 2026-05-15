package com.module.purchase.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    private String employeeName;

    private String employeeEmail;

    private String employeePhoneNumber;

    @ManyToOne
    @JoinColumn(name = "departmentId")
    @JsonIgnoreProperties({"employees","headEmployee"})
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roleId")
    @JsonIgnoreProperties({"employees"})
    private Role role;

    @Embedded
    private Address address;

    private Boolean active;

    @OneToOne
    @JoinColumn(name = "userId")
    @JsonIgnoreProperties({"employee"})
    private Users user;


    @OneToMany(mappedBy = "createdBy")
    @JsonIgnoreProperties({"createdBy"})
    private List<PurchaseRequestHeader> purchaseRequestHeaders;

    @OneToMany(mappedBy="createdBy")
    @JsonIgnoreProperties({"createdBy"})
    private List<PurchaseOrderHeader> purchaseOrderHeaders;

    @OneToMany(mappedBy = "approver")
    @JsonIgnoreProperties({"approver"})
    private List<AssigningApprovals> forApprovals;

    @OneToMany(mappedBy = "assignedBy")
    @JsonIgnoreProperties({"assignedBy"})
    private List<AssigningApprovals> assignedApprovals;

    @OneToMany(mappedBy = "performedBy")
    @JsonIgnoreProperties({"performedBy"})
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

    
}
