package com.module.purchase.view.user;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Users;
import com.module.purchase.entity.Vendor;
import com.module.purchase.enums.ViewName;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.UsersService;
import com.module.purchase.service.VendorService;
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
public class UsersEditView extends VerticalLayout implements HasUrlParameter<String> {

    private final UsersService usersService;
    private final SecurityService securityService;

    private Users user;

    private final TextField userNameField = new TextField("User Name");
    private final EmailField userEmailField = new EmailField("User Email");

    private final PasswordField passwordField = new PasswordField("New Password (optional)");

    private final ComboBox<String> userTypeField = new ComboBox<>("User Type");

    private final ComboBox<Employee> employeeField = new ComboBox<>("Employee");

    private final ComboBox<Vendor> vendorField = new ComboBox<>("Vendor");

    private final ComboBox<String> activeField = new ComboBox<>("Status");

    public UsersEditView(UsersService usersService, EmployeeService employeeService,
            VendorService vendorService, SecurityService securityService) {

        this.usersService = usersService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);

        userNameField.setPattern("[0-9a-zA-Z]{3,50}");
        userNameField.setRequired(true);
        userNameField.setErrorMessage("Enter a valid user name. Only letters, numbers");
        employeeField.setItems(employeeService.getEmployees());
        employeeField.setItemLabelGenerator(Employee::getEmployeeName);

        vendorField.setItems(vendorService.getVendors());
        vendorField.setItemLabelGenerator(Vendor::getVendorName);

        userTypeField.setItems("Employee", "Vendor");

        activeField.setItems("Active", "Inactive");
    }

    @Override
    public void setParameter(BeforeEvent event, String userId) {

        removeAll();
        try{
        if (securityService.getLoggedInUser().getUserId().equals(Long.parseLong(userId))
                || securityService.canAccessView("management-group")) {
            user = usersService.getUserById(Long.parseLong(userId)).orElse(null);
        } else {
            event.forwardTo("");
            event.getUI().access(() -> {
                Notification.show("Access Denied", 3000, Notification.Position.MIDDLE);
            });
        }

        if (user == null) {
            add(new H2("User Not Found"));
            return;
        }

        H2 title = new H2("Update User");

        userNameField.setValue(user.getUserName() == null ? "" : user.getUserName());

        userEmailField.setValue(user.getUserEmail() == null ? "" : user.getUserEmail());

        userEmailField.setReadOnly(true);

        if (user.getEmployee() != null) {

            userTypeField.setValue("Employee");
            employeeField.setValue(user.getEmployee());

            employeeField.setVisible(true);
            vendorField.setVisible(false);

        } else if (user.getVendor() != null) {

            userTypeField.setValue("Vendor");
            vendorField.setValue(user.getVendor());

            employeeField.setVisible(false);
            vendorField.setVisible(true);
        }

        userTypeField.setReadOnly(true);
        employeeField.setReadOnly(true);
        vendorField.setReadOnly(true);

        activeField.setValue(
                Boolean.TRUE.equals(user.getActive())
                        ? "Active"
                        : "Inactive");

        activeField.setReadOnly(!securityService.canAccessView("user-form"));

        FormLayout formLayout = new FormLayout();

        formLayout.add(
                userNameField,
                userEmailField,
                passwordField,
                userTypeField,
                employeeField,
                vendorField,
                activeField);

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2));

        Button saveButton = new Button("Update");

        saveButton.addClickListener(e -> {

            try {
                if (userNameField.isEmpty() || userEmailField.isEmpty() || passwordField.isEmpty()
                        || userTypeField.isEmpty()) {

                    Notification.show("Please fill all required fields", 3000, Notification.Position.TOP_CENTER);
                    return;
                }

                if (userNameField.isInvalid() || userEmailField.isInvalid()) {
                    Notification.show("Please correct validation errors", 3000, Notification.Position.TOP_CENTER);
                }
                user.setUserName(userNameField.getValue());

                user.setActive("Active".equals(activeField.getValue()));

                if (!passwordField.isEmpty()) {
                    user.setPassword(passwordField.getValue());
                } else {
                    user.setPassword(null);
                }

                usersService.updateUser(user,securityService.getLoggedInUser().getEmployee());
                Notification.show( "User Updated Successfully",3000, Notification.Position.TOP_CENTER);

                getUI().ifPresent(ui -> ui.navigate("user-details/" + user.getUserId()));

            }catch (Exception ex) {
            event.forwardTo("user");
            event.getUI().access(() -> {Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);});
            return;
        }
        });

        Button cancelButton = new Button("Cancel");

        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("user-details/" + user.getUserId())));

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        add(title, formLayout, buttons);
        }catch (NumberFormatException e) {
            event.forwardTo(ViewName.USER.getRoute());
            event.getUI().access(() -> {
                Notification.show("url is not valid ," + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
            });
            return;
        }catch (Exception ex) {
            event.forwardTo(ViewName.USER.getRoute());
            event.getUI().access(() -> {Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);});
            return;
        }
    }
}