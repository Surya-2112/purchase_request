package com.module.purchase.view.department;

import java.util.Optional;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Department;
import com.module.purchase.enums.ViewName;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

@Route(value = "department-details", layout = MainLayout.class)
@PermitAll
public class DepartmentDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    private final DepartmentService departmentService;

    private final SecurityService securityService;

    public DepartmentDetailsView(DepartmentService departmentService, SecurityService securityService) {

        this.departmentService = departmentService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent event,String departmentId) {

        removeAll();

        try {
            Optional<Department> optionalDepartment = departmentService.getDepartmentById(Long.parseLong(departmentId));

            if (optionalDepartment.isEmpty()) {
                add(new Span("Department not found"));
                return;
            }

            Department department = optionalDepartment.get();

            H2 title = new H2("Department Details");

            FormLayout formLayout = new FormLayout();

            formLayout.addFormItem(new Span(String.valueOf(department.getDepartmentId())), "Department ID");
            formLayout.addFormItem(new Span(department.getDepartmentName()), "Department Name");
            formLayout.addFormItem(new Span(department.getDepartmentCode()), "Department Code");
            formLayout.addFormItem(
                    new Span(
                            department.getHeadEmployee() == null ? "" : department.getHeadEmployee().getEmployeeName()),
                    "Department Head");
            formLayout.addFormItem(new Span(department.getActive() ? "Active" : "Inactive"), "Status");

            Button updateButton = new Button("Update");

            updateButton.addClickListener(clickEvent -> {
                getUI().ifPresent(ui -> ui.navigate("department-edit/" + department.getDepartmentId()));
            });

            Button deleteButton = new Button("Delete");

            deleteButton.addClickListener(clickEvent -> {

                ConfirmDialog dialog = new ConfirmDialog();

                dialog.setHeader("Delete Department");

                dialog.setText("Are you sure you want to delete this department?");

                dialog.setCancelable(true);

                dialog.setConfirmText("Delete");

                dialog.setConfirmButtonTheme("error primary");

                dialog.addConfirmListener(confirmEvent -> {

                    try {
                        departmentService.deleteDepartmentById(department.getDepartmentId(),
                                securityService.getLoggedInUser().getEmployee());
                        Notification.show("Department Deleted Successfully");
                        getUI().ifPresent(ui -> ui.navigate("department"));

                    } catch (Exception exception) {
                        Notification.show(exception.getMessage(), 5000, Notification.Position.TOP_CENTER);
                    }

                });

                dialog.open();
            });

            updateButton.setVisible(securityService.canAccessView("department-edit"));

            deleteButton.setVisible(securityService.canAccessView("department-form"));
            updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
            deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_ERROR);

            HorizontalLayout buttonLayout = new HorizontalLayout(updateButton, deleteButton);

            add(title, formLayout, buttonLayout);
        } catch (NumberFormatException e) {
            event.forwardTo(ViewName.DEPARTMENT.getRoute());
            event.getUI().access(() -> {
                Notification.show("url is not valid ," + e.getMessage(), 3000,
                        Notification.Position.TOP_CENTER);
            });
            return;
        } catch (Exception ex) {
            event.forwardTo(ViewName.DEPARTMENT.getRoute());
            event.getUI().access(() -> {
                Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            });
            return;
        }
    }
}