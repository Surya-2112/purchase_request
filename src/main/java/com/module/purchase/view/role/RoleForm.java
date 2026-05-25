package com.module.purchase.view.role;

import java.util.List;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Role;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.service.RoleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class RoleForm extends Dialog {

    private final RoleService roleService;

    private final SecurityService securityService;


    // FIELDS
    private final TextField roleNameField = new TextField("Role Name");

    private final CheckboxGroup<EmployeeGroup> employeeGroupField = new CheckboxGroup<>();

    public RoleForm(RoleService roleService,SecurityService securityService) {

        this.roleService = roleService;
        this.securityService = securityService;

        setHeaderTitle("Add Role");

        setWidth("700px");

        // REQUIRED
        roleNameField.setRequiredIndicatorVisible(true);

        employeeGroupField.setLabel("Employee Groups");

        employeeGroupField.setRequiredIndicatorVisible(true);

        // LOAD EMPLOYEE GROUPS
        employeeGroupField.setItems(
                EmployeeGroup.values());

        // FORM
        FormLayout formLayout =
                new FormLayout();

        formLayout.add(
                roleNameField,
                employeeGroupField);

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2));

        // BUTTONS
        Button saveButton =
                new Button("Save");

        Button cancelButton =
                new Button("Cancel");

        saveButton.addClickListener(
                event -> saveRole());

        cancelButton.addClickListener(
                event -> close());

        HorizontalLayout buttonLayout =
                new HorizontalLayout(
                        saveButton,
                        cancelButton);

        add(formLayout, buttonLayout);
    }

    private void saveRole() {

        try {

            // VALIDATION
            if (roleNameField.isEmpty()
                    || employeeGroupField.getValue().isEmpty()) {

                Notification.show(
                        "Please fill all required fields",
                        3000,
                        Notification.Position.TOP_CENTER);

                return;
            }

            Role role =
                    new Role();

            // SET VALUES
            role.setRoleName(
                    roleNameField.getValue());

            role.setEmployeeGroups(
                    List.copyOf(
                            employeeGroupField.getValue()));

            // SAVE
            roleService.addRole(role,securityService.getLoggedInUser().getEmployee());

            Notification.show(
                    "Role Saved Successfully",
                    3000,
                    Notification.Position.TOP_CENTER);

            close();

        } catch (Exception exception) {

            Notification.show(
                    "Error : "
                            + exception.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER);
        }
    }
}