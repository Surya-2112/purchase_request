package com.module.purchase.enums;

public enum Action {
    CREATE("Create"),
    UPDATE("Update"),
    DELETE("Delete"),
    APPROVE("Approved"),
    PARTIALLY_APPROVED("Partially Approved"),
    REJECT("Rejected"),
    CANCEL("Cancel");

    private final String displayName;

    Action(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
