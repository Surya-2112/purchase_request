package com.module.purchase.view.employee;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Employee;
import com.module.purchase.service.EmployeeService;
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

@Route(value = "employee-details", layout = MainLayout.class)
@PermitAll
public class EmployeeDetailsView extends VerticalLayout
        implements HasUrlParameter<Long> {

    private final EmployeeService employeeService;

    private final SecurityService securityService;

    public EmployeeDetailsView(EmployeeService employeeService,SecurityService securityService) {

        this.employeeService = employeeService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent event, Long employeeId) {

        removeAll();

        Employee employee = employeeService.getEmployeeById(employeeId).get();

        if (employee == null) {

            add(new Span("Employee not found"));
            return;
        }

        H2 title = new H2("Employee Details");

        FormLayout formLayout = new FormLayout();

        formLayout.addFormItem(
                new Span(String.valueOf(employee.getEmployeeId())),
                "Employee ID");

        formLayout.addFormItem(
                new Span(employee.getEmployeeName()),
                "Employee Name");

        formLayout.addFormItem(
                new Span(employee.getEmployeeEmail()),
                "Email");

        formLayout.addFormItem(
                new Span(employee.getEmployeePhoneNumber()),
                "Phone");

        formLayout.addFormItem(
                new Span(
                        employee.getDepartment() == null
                                ? ""
                                : employee.getDepartment().getDepartmentName()),
                "Department");

        formLayout.addFormItem(
                new Span(
                        employee.getRole() == null
                                ? ""
                                : employee.getRole().getRoleName()),
                "Role");

        formLayout.addFormItem(
                new Span(
                        employee.getActive() ? "Active" : "Inactive"),
                "Status");

        if (employee.getAddress() != null) {

            formLayout.addFormItem(
                    new Span(employee.getAddress().getStreet()),
                    "Street");

            formLayout.addFormItem(
                    new Span(employee.getAddress().getCity()),
                    "City");

            formLayout.addFormItem(
                    new Span(employee.getAddress().getState()),
                    "State");

            formLayout.addFormItem(
                    new Span(employee.getAddress().getCountry()),
                    "Country");

            formLayout.addFormItem(
                    new Span(employee.getAddress().getPostalCode()),
                    "Pincode");
        }

        Button updateButton = new Button("Update");

        updateButton.addClickListener(clickEvent -> {

            getUI().ifPresent(ui -> ui.navigate("employee-edit/" + employee.getEmployeeId()));

        });

        Button deleteButton = new Button("Delete");

        deleteButton.addClickListener(clickEvent -> {

            ConfirmDialog dialog = new ConfirmDialog();

            dialog.setHeader("Delete Employee");

            dialog.setText("Are you sure you want to delete this employee?");

            dialog.setCancelable(true);

            dialog.setConfirmText("Delete");

            dialog.setConfirmButtonTheme("error primary");

            dialog.addConfirmListener(confirmEvent -> {

                try {

                    employeeService.deleteEmployeeById(employee.getEmployeeId(),securityService.getLoggedInUser().getEmployee());

                    Notification.show(
                            "Employee Deleted Successfully");

                    getUI().ifPresent(ui -> ui.navigate("employee"));

                } catch (Exception exception) {

                    Notification.show(
                            exception.getMessage(),
                            5000,
                            Notification.Position.TOP_CENTER);
                }

            });

            dialog.open();
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(updateButton, deleteButton);

        add(title, formLayout, buttonLayout);
    }
}