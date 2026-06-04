package com.module.purchase.enums;

public enum RepeatedPeriodReferType {

    PURCHASE_REQUEST_LINE("Purchase Request Line"),
    CATEGORY("Category");

    private final String displayName;

    RepeatedPeriodReferType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}