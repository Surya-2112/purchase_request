package com.module.purchase.enums;

import java.util.Arrays;
import java.util.List;

public enum EmployeeGroup {
    
    SUPER_ADMIN("Super Admin"),
    ADMIN("Admin"),
    MANAGER("Manager"),
    EMPLOYEE("Employee"),
    INTERN("Intern"),
    FINANCE("Finance"),
    PURCHASE("Purchase"),
    HR("HR"),
    DIRECTOR("Director"),
    DEPARTMENT_HEAD("Department Head"),
    AUDITOR("Auditor"),
    VENDOR("Vendor");

    private final String displayName;

    EmployeeGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Returns only the groups allowed to approve documents
    public static List<EmployeeGroup> getApprovalGroups() {
        return Arrays.asList(
            SUPER_ADMIN,
            ADMIN,
            MANAGER,
            FINANCE,
            PURCHASE,
            HR,
            DIRECTOR,
            DEPARTMENT_HEAD
        );
    }
}