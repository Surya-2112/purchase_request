package com.module.purchase.enums;

public enum RequestForQuotationStatus {

    DRAFT("Draft"),
    OPEN("Open"),
    CLOSED("Closed"),
    CANCELLED("Cancelled");

    private final String displayName;

    RequestForQuotationStatus(String displayName) {
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