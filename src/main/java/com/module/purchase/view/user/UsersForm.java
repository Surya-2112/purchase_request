package com.module.purchase.view.user;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Users;
import com.module.purchase.entity.Vendor;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.UsersService;
import com.module.purchase.service.VendorService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

    private final ComboBox<String> userTypeField = new ComboBox<>("User Type");

    private final ComboBox<Employee> employeeField = new ComboBox<>("Employee");
    private final ComboBox<Vendor> vendorField = new ComboBox<>("Vendor");

    public UsersForm( UsersService usersService,EmployeeService employeeService,VendorService vendorService, SecurityService securityService) {

        this.usersService = usersService;
        this.securityService = securityService;

        setHeaderTitle("Add User");
        setWidth("600px");

        userTypeField.setItems("Employee", "Vendor");
        userTypeField.setRequired(true);

        employeeField.setItems(employeeService.getEmplyeesWithoutUsers());
        employeeField.setItemLabelGenerator(Employee::getEmployeeName);
        employeeField.setVisible(false);

        vendorField.setItems(vendorService.getVendorsWithoutUser());
        vendorField.setItemLabelGenerator(Vendor::getVendorName);
        vendorField.setVisible(false);

        userTypeField.addValueChangeListener(event -> {

            employeeField.clear();
            vendorField.clear();

            if("Employee".equals(event.getValue())) {
                employeeField.setVisible(true);
                vendorField.setVisible(false);
            } else if ("Vendor".equals(event.getValue())) {
                employeeField.setVisible(false);
                vendorField.setVisible(true);
            } else {
                employeeField.setVisible(false);
                vendorField.setVisible(false);
            }
        });

        userNameField.setPattern("[0-9a-zA-Z]{3,50}");
        userNameField.setRequired(true);
        userNameField.setMaxLength(50);
        userNameField.setErrorMessage("Enter a valid user name.Only letters, numbers");
        userEmailField.setRequired(true); 
        userEmailField.setErrorMessage("Enter a valid email");
        passwordField.setRequired(true);
        confirmPasswordField.setRequired(true);

        FormLayout formLayout = new FormLayout();

        formLayout.add(
                userNameField,
                userEmailField,
                passwordField,
                confirmPasswordField,
                userTypeField,
                employeeField,
                vendorField
        );

        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        Button saveButton = new Button("Save", e -> saveUser());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);

        Button cancelButton = new Button("Cancel", e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_ERROR);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);

        add(formLayout, buttonLayout);
    }

    private void saveUser() {

        try {

            if (userNameField.isEmpty() || userEmailField.isEmpty() || passwordField.isEmpty()
                    || confirmPasswordField.isEmpty() || userTypeField.isEmpty()) {

                Notification.show( "Please fill all required fields",3000,Notification.Position.TOP_CENTER);
                return;
            }

            if(userNameField.isInvalid() || userEmailField.isInvalid())
            {
                Notification.show("Please correct validation errors",3000,Notification.Position.TOP_CENTER);
            }

            if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {

                Notification.show("Password and Confirm Password do not match",3000,Notification.Position.TOP_CENTER);
                return;
            }

            Users user = new Users();

            user.setUserName(userNameField.getValue().trim());
            user.setUserEmail(userEmailField.getValue().trim());
            user.setPassword(passwordField.getValue());
            user.setActive(true);

            if ("Employee".equals(userTypeField.getValue())) {

                if (employeeField.isEmpty()) {

                    Notification.show("Please select an employee",3000,Notification.Position.TOP_CENTER);
                    return;
                }

                user.setEmployee(employeeField.getValue());

            } else if ("Vendor".equals(userTypeField.getValue())) {

                if (vendorField.isEmpty()) {

                    Notification.show("Please select a vendor",3000,Notification.Position.TOP_CENTER);
                    return;
                }
                user.setVendor(vendorField.getValue());
            }

            usersService.addUsers( user, securityService.getLoggedInUser().getEmployee());

            Notification.show( "User Created Successfully", 3000, Notification.Position.TOP_CENTER);

            close();

        } catch (Exception e) {

            Notification.show("Error: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER);
        }
    }
}