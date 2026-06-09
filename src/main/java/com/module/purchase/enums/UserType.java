package com.module.purchase.enums;

public enum  UserType {
    
    EMPLOYEE("Employee"),
    VENDOR("Vendor");

    private final String displayName;

    UserType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
