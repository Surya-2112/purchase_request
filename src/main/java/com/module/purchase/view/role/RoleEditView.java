package com.module.purchase.view.role;

import java.util.List;
import java.util.Set;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Role;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.service.RoleService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "role-edit", layout = MainLayout.class)
@PermitAll
public class RoleEditView extends VerticalLayout
        implements HasUrlParameter<Long> {

    private final RoleService roleService;

    private final SecurityService securityService;

    // FIELDS
    private final TextField roleNameField =
            new TextField("Role Name");

    private final CheckboxGroup<EmployeeGroup> employeeGroupField =
            new CheckboxGroup<>();

    private Role role;

    public RoleEditView(RoleService roleService, SecurityService securityService) {

        this.roleService = roleService;
        this.securityService=securityService;

        setSizeFull();

        setPadding(true);

        employeeGroupField.setLabel(
                "Employee Groups");

        // LOAD EMPLOYEE GROUPS
        employeeGroupField.setItems(
                EmployeeGroup.values());
    }

    @Override
    public void setParameter(
            BeforeEvent event,
            Long roleId) {

        removeAll();

        role =
                roleService
                        .getRoleById(roleId)
                        .orElse(null);

        if (role == null) {

            add(new H2("Role Not Found"));

            return;
        }

        H2 title =
                new H2("Update Role");

        // SET VALUES
        roleNameField.setValue(
                role.getRoleName() == null
                        ? ""
                        : role.getRoleName());

        if (role.getEmployeeGroups() != null) {

            employeeGroupField.setValue( Set.copyOf(role.getEmployeeGroups()));
        }

        // FORM
        FormLayout formLayout =
                new FormLayout();

        formLayout.add(
                roleNameField,
                employeeGroupField);

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2));

        // SAVE BUTTON
        Button saveButton =
                new Button("Save");

        saveButton.addClickListener(clickEvent -> {

            try {

                // VALIDATION
                if (roleNameField.isEmpty()
                        || employeeGroupField.isEmpty()) {

                    Notification.show(
                            "Please fill all required fields",
                            3000,
                            Notification.Position.TOP_CENTER);

                    return;
                }

                // UPDATE VALUES
                role.setRoleName(
                        roleNameField.getValue());

                role.setEmployeeGroups(
                        List.copyOf(
                                employeeGroupField.getValue()));

                // UPDATE
                roleService.updateRole(role,securityService.getLoggedInUser().getEmployee());

                Notification.show(
                        "Role Updated Successfully",
                        3000,
                        Notification.Position.TOP_CENTER);

                getUI().ifPresent(ui ->
                        ui.navigate(
                                "role-details/"
                                        + role.getRoleId()));

            } catch (Exception exception) {

                Notification.show(
                        exception.getMessage(),
                        5000,
                        Notification.Position.TOP_CENTER);
            }

        });

        // CANCEL BUTTON
        Button cancelButton =
                new Button("Cancel");

        cancelButton.addClickListener(clickEvent -> {

            getUI().ifPresent(ui ->
                    ui.navigate(
                            "role-details/"
                                    + role.getRoleId()));

        });

        HorizontalLayout buttonLayout =
                new HorizontalLayout(
                        saveButton,
                        cancelButton);

        add(
                title,
                formLayout,
                buttonLayout);
    }
}