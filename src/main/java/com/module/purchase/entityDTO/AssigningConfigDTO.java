package com.module.purchase.entityDTO;

import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;

public class AssigningConfigDTO {
    
    private Long id;

    private ApprovalType approvalType;

    private Integer level;  

    private EmployeeGroup employeeGroup;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApprovalType getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(ApprovalType approvalType) {
        this.approvalType = approvalType;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public EmployeeGroup getEmployeeGroup() {
        return employeeGroup;
    }

    public void setEmployeeGroup(EmployeeGroup employeeGroup) {
        this.employeeGroup = employeeGroup;
    }


}
