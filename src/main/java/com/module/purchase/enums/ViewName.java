package com.module.purchase.enums;

public enum ViewName {

    DASHBOARD(""),
    LOGIN("login"),
    ACCESS_DENIED("access-denied"),
    VIEW_PERMISSION("view-permission"),

    ASSIGNING_CONFIG("assigning-config"),
    ASSIGNING_CONFIG_DETAILS("assigning-config-details"),
    ASSIGNING_CONFIG_EDIT("assigning-config-edit"),
    ASSIGNING_CONFIG_FORM("assigning-config-form"),

    DEPARTMENT("department"),
    DEPARTMENT_DETAILS("department-details"),
    DEPARTMENT_EDIT("department-edit"),
    DEPARTMENT_FORM("department-form"),

    DEPARTMENT_BUDGET("department-budget"),
    DEPARTMENT_BUDGET_DETAILS("department-budget-details"),
    DEPARTMENT_BUDGET_EDIT("department-budget-edit"),
    DEPARTMENT_BUDGET_FORM("department-budget-form"),

    EMPLOYEE("employee"),
    EMPLOYEE_DETAILS("employee-details"),
    EMPLOYEE_EDIT("employee-edit"),
    EMPLOYEE_FORM("employee-form"),

    ITEM("item"),
    ITEM_DETAILS("item-details"),
    ITEM_EDIT("item-edit"),
    ITEM_FORM("item-form"),

    PURCHASE_REQUEST("purchase-request"),
    PURCHASE_REQUEST_FORM("purchase-request-form"),
    PURCHASE_REQUEST_DETAILS("purchase-request-details"),
    PURCHASE_REQUEST_APPROVAL("purchase-request-approval"),
    ASSIGNING_APPROVALS_REQUEST_DETAILS("assigning-approvals-details"),

    PURCHASE_ORDER("purchase-order"),
    PURCHASE_ORDER_FORM("purchase-order-form"),
    PURCHASE_ORDER_DETAILS("purchase-order-details"),
    PURCHASE_ORDER_APPROVAL("purchase-order-approval"),
    PURCHASE_ORDER_DRAFT("purchase-order-draft"),
    ASSIGNING_APPROVALS_ORDERS_DETAILS("assigning-approvals-orders-details"),

    USER("user"),
    USER_DETAILS("user-details"),
    USER_EDIT("user-edit"),
    USER_FORM("user-form"),

    ROLE("role"),
    ROLE_DETAILS("role-details"),
    ROLE_EDIT("role-edit"),
    ROLE_FORM("role-form"),

    VENDOR("vendor"),
    VENDOR_DETAILS("vendor-details"),
    VENDOR_EDIT("vendor-edit"),
    VENDOR_FORM("vendor-form"),

    VENDOR_CATEGORY("vendor-category"),
    VENDOR_CATEGORY_DETAILS("vendor-category-details"),
    VENDOR_CATEGORY_EDIT("vendor-category-edit"),
    VENDOR_CATEGORY_FORM("vendor-category-form"),
    
    AUDIT_LOGS("audit-logs");

    private final String route;

    ViewName(String route) {
        this.route = route;
    }

    public String getRoute() {
        return route;
    }
}