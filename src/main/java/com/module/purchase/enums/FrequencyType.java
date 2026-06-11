package com.module.purchase.enums;

import java.time.LocalDate;

public enum FrequencyType {

    TIME("Time"),
    DAYS("Days"),
    WEEKS("Weeks"),
    MONTHS("Months"),
    YEARS("Years");

    private final String displayName;

    FrequencyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public LocalDate calculateNext(LocalDate current, int period) {
        return switch (this) {
            case DAYS -> current.plusDays(period);
            case WEEKS -> current.plusWeeks(period);
            case MONTHS -> current.plusMonths(period);
            case YEARS -> current.plusYears(period);
            default -> throw new IllegalStateException("Unexpected value: " + (this));
        };
}
}