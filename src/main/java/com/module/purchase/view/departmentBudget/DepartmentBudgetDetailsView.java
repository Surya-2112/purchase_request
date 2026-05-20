package com.module.purchase.view.departmentBudget;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.service.DepartmentBudgetService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "department-budget-details", layout = MainLayout.class)
@PermitAll
public class DepartmentBudgetDetailsView extends VerticalLayout
        implements HasUrlParameter<Long> {

    private final DepartmentBudgetService departmentBudgetService;

    private final SecurityService securityService;

    public DepartmentBudgetDetailsView(DepartmentBudgetService departmentBudgetService, SecurityService securityService) {

        this.departmentBudgetService = departmentBudgetService;
        this.securityService=securityService;

        setSizeFull();

        setPadding(true);

        setSpacing(true);
    }

    @Override
    public void setParameter(
            BeforeEvent event,
            Long departmentBudgetId) {

        removeAll();

        DepartmentBudget departmentBudget =
                departmentBudgetService
                        .getDepartmentBudgetById(
                                departmentBudgetId)
                        .orElse(null);

        if (departmentBudget == null) {

            add(new Span("Department Budget Not Found"));

            return;
        }

        H2 title = new H2("Department Budget Details");

        FormLayout formLayout = new FormLayout();

        // BUDGET ID
        formLayout.addFormItem(
                new Span(
                        String.valueOf(
                                departmentBudget
                                        .getDepartmentBudgetId())),
                "Department Budget ID");

        // DEPARTMENT
        formLayout.addFormItem(
                new Span(
                        departmentBudget.getDepartment() == null
                                ? ""
                                : departmentBudget
                                        .getDepartment()
                                        .getDepartmentName()),
                "Department");

        // TOTAL BUDGET
        formLayout.addFormItem(
                new Span(
                        String.valueOf(
                                departmentBudget
                                        .getTotalBudgetAmount())),
                "Total Budget Amount");

        // REMAINING BUDGET
        formLayout.addFormItem(
                new Span(
                        String.valueOf(
                                departmentBudget
                                        .getRemainingBudgetAmount())),
                "Remaining Budget Amount");

        // YEAR
        formLayout.addFormItem(
                new Span(
                        departmentBudget.getYear() == null
                                ? ""
                                : departmentBudget
                                        .getYear()
                                        .toString()),
                "Year");

        // UPDATE BUTTON
        Button updateButton =
                new Button("Update");

        updateButton.addClickListener(clickEvent -> {

            getUI().ifPresent(ui ->
                    ui.navigate(
                            "department-budget-edit/"
                                    + departmentBudget
                                            .getDepartmentBudgetId()));
        });

        // DELETE BUTTON
        Button deleteButton =
                new Button("Delete");

        deleteButton.addClickListener(clickEvent -> {

            ConfirmDialog dialog =
                    new ConfirmDialog();

            dialog.setHeader(
                    "Delete Department Budget");

            dialog.setText(
                    "Are you sure you want to delete this department budget?");

            dialog.setCancelable(true);

            dialog.setConfirmText("Delete");

            dialog.setConfirmButtonTheme(
                    "error primary");

            dialog.addConfirmListener(confirmEvent -> {

                try {

                    departmentBudgetService
                            .deleteDepartmentBudgetById(departmentBudget.getDepartmentBudgetId(),securityService.getLoggedInUser().getEmployee());

                    Notification.show(
                            "Department Budget Deleted Successfully");

                    getUI().ifPresent(ui ->
                            ui.navigate(
                                    "department-budget"));

                } catch (Exception exception) {

                    Notification.show(
                            exception.getMessage(),
                            5000,
                            Notification.Position.TOP_CENTER);
                }

            });

            dialog.open();
        });

        HorizontalLayout buttonLayout =
                new HorizontalLayout(
                        updateButton,
                        deleteButton);

        add(title, formLayout, buttonLayout);
    }
}