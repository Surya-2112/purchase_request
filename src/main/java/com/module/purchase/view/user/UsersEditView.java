package com.module.purchase.view.user;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Users;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.UsersService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "user-edit", layout = MainLayout.class)
@PermitAll
public class UsersEditView extends VerticalLayout implements HasUrlParameter<Long> {

    private final UsersService usersService;
    private final EmployeeService employeeService;
    private final SecurityService securityService;


    private Users user;

    private final TextField userNameField = new TextField("User Name");
    private final EmailField userEmailField = new EmailField("User Email");

    private final PasswordField passwordField = new PasswordField("New Password (optional)");

    private final ComboBox<Employee> employeeField = new ComboBox<>("Employee");

    private final ComboBox<String> activeField = new ComboBox<>("Status");

    public UsersEditView(UsersService usersService, EmployeeService employeeServices, SecurityService securityService) {

        this.usersService = usersService;
        this.employeeService = employeeServices;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);

        employeeField.setItems(employeeService.getEmployees());
        employeeField.setItemLabelGenerator(Employee::getEmployeeName);

        activeField.setItems("Active", "Inactive");
    }

    @Override
    public void setParameter(BeforeEvent event, Long userId) {

        removeAll();

        user = usersService.getUserById(userId).orElse(null);

        if (user == null) {
            add(new H2("User Not Found"));
            return;
        }

        H2 title = new H2("Update User");

        userNameField.setValue(user.getUserName() == null ? "" : user.getUserName());
        userEmailField.setValue(user.getUserEmail() == null ? "" : user.getUserEmail());
        userEmailField.setReadOnly(true);

        employeeField.setValue(user.getEmployee());
        employeeField.setReadOnly(true);

        activeField.setValue(Boolean.TRUE.equals(user.getActive()) ? "Active" : "Inactive");

        activeField.setReadOnly(!securityService.canAccessView("user-form"));
        FormLayout formLayout = new FormLayout();

        formLayout.add(
                userNameField,
                userEmailField,
                passwordField,
                employeeField,
                activeField
        );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2)
        );

        Button saveButton = new Button("Update");

        saveButton.addClickListener(e -> {

            try {

                user.setUserName(userNameField.getValue());
                user.setUserEmail(userEmailField.getValue());

                user.setEmployee(employeeField.getValue());

                user.setActive(activeField.getValue() != null && activeField.getValue().equals("Active"));

                if (!passwordField.isEmpty()) {
                    user.setPassword(passwordField.getValue());
                }else{
                    user.setPassword(null);
                }

                usersService.updateUser(user,securityService.getLoggedInUser().getEmployee());

                Notification.show(
                        "User Updated Successfully",
                        3000,
                        Notification.Position.TOP_CENTER
                );

                getUI().ifPresent(ui ->
                        ui.navigate("user-details/" + user.getUserId())
                );

            } catch (Exception ex) {

                Notification.show(
                        ex.getMessage(),
                        5000,
                        Notification.Position.TOP_CENTER
                );
            }
        });

        // CANCEL
        Button cancelButton = new Button("Cancel");

        cancelButton.addClickListener(e ->
                getUI().ifPresent(ui ->
                        ui.navigate("user-details/" + user.getUserId())
                )
        );

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        add(title, formLayout, buttons);
    }
}