package com.module.purchase.view.user;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Users;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.UsersService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

public class UsersForm extends Dialog {

    private final UsersService usersService;

    private final SecurityService securityService;

    private final TextField userNameField = new TextField("User Name");
    private final EmailField userEmailField = new EmailField("User Email");

    private final PasswordField passwordField = new PasswordField("Password");
    private final PasswordField confirmPasswordField = new PasswordField("Confirm Password");

    private final ComboBox<Employee> employeeField = new ComboBox<>("Employee");


    public UsersForm(UsersService usersService, EmployeeService employeeService,SecurityService securityService ) {

        this.usersService = usersService;
        this.securityService = securityService;

        setHeaderTitle("Add User");
        setWidth("600px");

        employeeField.setItems(employeeService.getEmployees());
        employeeField.setItemLabelGenerator(Employee::getEmployeeName);

        userNameField.setRequired(true);
        userEmailField.setRequired(true);
        passwordField.setRequired(true);
        confirmPasswordField.setRequired(true);
        employeeField.setRequired(true);

        FormLayout formLayout = new FormLayout();

        formLayout.add(
                userNameField,
                userEmailField,
                passwordField,
                confirmPasswordField,
                employeeField
        );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2)
        );

        Button saveButton = new Button("Save", e -> saveUser());
        Button cancelButton = new Button("Cancel", e -> close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);

        add(formLayout, buttonLayout);
    }

    private void saveUser() {

        try {

            if (userNameField.isEmpty()
                    || userEmailField.isEmpty()
                    || passwordField.isEmpty()
                    || confirmPasswordField.isEmpty()
                    || employeeField.isEmpty()) {

                Notification.show("Please fill all required fields");
                return;
            }

            if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
                Notification.show("Password and Confirm Password do not match");
                return;
            }

            Users user = new Users();

            user.setUserName(userNameField.getValue());
            user.setUserEmail(userEmailField.getValue());
            user.setEmployee(employeeField.getValue());
            user.setActive(true);

            user.setPassword(passwordField.getValue());

            usersService.addUsers(user,securityService.getLoggedInUser().getEmployee());

            Notification.show(
                    "User Created Successfully",
                    3000,
                    Notification.Position.TOP_CENTER
            );

            close();

        } catch (Exception e) {

            Notification.show(
                    "Error: " + e.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
            );
        }
    }
}