package com.module.purchase.view.role;

import java.util.List;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Role;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.service.RoleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class RoleForm extends Dialog {

    private final RoleService roleService;

    private final SecurityService securityService;

    private final TextField roleNameField = new TextField("Role Name");

    private final CheckboxGroup<EmployeeGroup> employeeGroupField = new CheckboxGroup<>();

    public RoleForm(RoleService roleService,SecurityService securityService) {

        this.roleService = roleService;
        this.securityService = securityService;

        setHeaderTitle("Add Role");

        setWidth("700px");

        roleNameField.setRequiredIndicatorVisible(true);
        roleNameField.setPattern("[a-zA-Z]{2,50}");
        roleNameField.setErrorMessage("Enter a valid role name");
        roleNameField.setMaxLength(50);

        employeeGroupField.setLabel("Role Groups");

        employeeGroupField.setRequiredIndicatorVisible(true);

        employeeGroupField.setItems(EmployeeGroup.values());

        FormLayout formLayout =new FormLayout();

        formLayout.add(roleNameField,employeeGroupField);

        formLayout.setResponsiveSteps( new FormLayout.ResponsiveStep("0", 2));

        Button saveButton =new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);

        Button cancelButton = new Button("Cancel");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_ERROR);

        saveButton.addClickListener(event -> saveRole());

        cancelButton.addClickListener( event -> close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);

        add(formLayout, buttonLayout);
    }

    private void saveRole() {
        try {
            if (roleNameField.isEmpty() || employeeGroupField.getValue().isEmpty()) {
                Notification.show( "Please fill all required fields", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            if(roleNameField.isInvalid())
            {
                Notification.show("Please correct validation errors",3000,Notification.Position.TOP_CENTER);
            }

            Role role = new Role();
            role.setRoleName( roleNameField.getValue());
            role.setEmployeeGroups( List.copyOf( employeeGroupField.getValue()));
            roleService.addRole(role,securityService.getLoggedInUser().getEmployee());
            Notification.show("Role Saved Successfully", 3000, Notification.Position.TOP_CENTER);
            close();

        } catch (Exception exception) {
            Notification.show( "Error : "+ exception.getMessage(), 5000, Notification.Position.TOP_CENTER);
        }
    }
}