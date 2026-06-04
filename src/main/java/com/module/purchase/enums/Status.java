package com.module.purchase.enums;

public enum Status {

    DRAFT("Draft"),
    WAITING_APPROVAL("Waiting Approval"),
    APPROVED("Approved"),
    PARTIALLY_APPROVED("Partially Approved"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled"),
    ORDERED("Ordered");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
