package com.module.purchase.enums;

public enum ViewName {

    DASHBOARD(""),

    ASSIGNING_CONFIG("assigning-config"),
    ASSIGNING_CONFIG_DETAILS("assigning-config-details"),
    ASSIGNING_CONFIG_EDIT("assigning-config-edit"),
    ASSIGNING_CONFIG_FORM("assigning-config-form"),

    CATEGORY("category"),
    CATEGORY_DETAILS("category-details"),
    CATEGORY_EDIT("category-edit"),
    CATEGORY_FORM("category-form"),

    VENDOR("vendor"),
    VENDOR_DETAILS("vendor-details"),
    VENDOR_EDIT("vendor-edit"),
    VENDOR_FORM("vendor-form"),

    UNIT("unit"),
    UNIT_DETAILS("unit-details"),
    UNIT_EDIT("unit-edit"),
    UNIT_FORM("unit-form"),

    ITEM("item"),
    ITEM_DETAILS("item-details"),
    ITEM_EDIT("item-edit"),
    ITEM_FORM("item-form"),

    ITEM_VARIANT("item-variant"),
    ITEM_VARIANT_DETAILS("item-variant-details"),
    ITEM_VARIANT_EDIT("item-variant-edit"),
    ITEM_VARIANT_FORM("item-variant-form"),

    PURCHASE_REQUEST("purchase-request"),
    PURCHASE_REQUEST_FORM("purchase-request-form"),
    PURCHASE_REQUEST_DETAILS("purchase-request-details"),
    PURCHASE_REQUEST_APPROVAL("purchase-request-approval"),
    ASSIGNED_APPROVALS_REQUEST_DETAILS("assigned-approvals-details"),

    REQUEST_FOR_QUOTATION("request-for-quotation"),
    REQUEST_FOR_QUOTATION_FORM("rfq-form"),
    REQUEST_FOR_QUOTATION_DETAILS("request-for-quotation-details"),
    REQUEST_FOR_QUOTATION_FINALIZED("rfq-finalized-view"),

    QUOTATION_LEDGER("quotations"),
    QUOTATION_FORM("quotation-form"),
    QUOTATION_DETAILS("quotation-details"),
    QUOTATION_COMPARISON_DASHBOARD("quotation-comparison"),
    QUOTATION_EVALUATION_MATRIX("quotation-evaluation-matrix"),

    PURCHASE_ORDER("purchase-order"),
    PURCHASE_ORDER_DETAILS("purchase-order-details"),
    PURCHASE_ORDER_APPROVAL("purchase-order-approval"), 
    ASSIGNED_APPROVALS_ORDER_DETAILS("assigned-order-approvals-details"),

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
  
    USER("user"),
    USER_DETAILS("user-details"),
    USER_EDIT("user-edit"),
    USER_FORM("user-form"),

    ROLE("role"),
    ROLE_DETAILS("role-details"),
    ROLE_EDIT("role-edit"),
    ROLE_FORM("role-form"),

    REPEATED_PERIOD("repeated-periods"),
    REPEATED_PERIOD_DETAILS("repeated-period-details"),
    REPEATED_PERIOD_EDIT("repeated-period-edit"),
    
    AUDIT_LOGS("audit-logs"),

    PURCHASE_REQUEST_DEPARTMENT(""),

    MANAGEMENT_GROUP("management-group");

    private final String route;

    ViewName(String route) {
        this.route = route;
    }

    public String getRoute() {
        return route;
    }
}