package com.module.purchase.enums;

public enum EntityType {
    
    ASSIGNING_CONFIG("Assigning Configuration"),
    ASSIGNING_APPROVAL("Assigning Approval"),
    USER("User"),
    ROLE("Role"),
    EMPLOYEE("Employee"),
    DEPARTMENT("Department"),
    DEPARTMENT_BUDGET("Department Budget"),
    CATEGORY("Category"),
    ITEM("Item"),
    ITEM_VARIANT("Item Variant"),
    UNIT("Unit"),
    VENDOR("Vendor"),
    PURCHASE_REQUEST("Purchase Request"),
    REQUEST_FOR_QUOTATION("Request for Quotation"),
    QUOTATION("Quotation"),
    PURCHASE_ORDER("Purchase Order"),
    AUDIT_LOGS("Audit Logs"),
    VIEW_PERMISSION("View Permission"),
    REPEATED_PERIOD("Repeated Period");

    private final String displayName;

    EntityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
