package com.module.purchase.view.employee;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Address;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Role;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.RoleService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "employee-edit", layout = MainLayout.class)
@PermitAll
public class EmployeeEditView extends VerticalLayout
                implements HasUrlParameter<Long> {

        private final EmployeeService employeeService;

        private final SecurityService securityService;

        private final TextField employeeNameField = new TextField("Employee Name");

        private final EmailField employeeEmailField = new EmailField("Employee Email");

        private final TextField phoneField = new TextField("Phone Number");

        private final ComboBox<Department> departmentField = new ComboBox<>("Department");

        private final ComboBox<Role> roleField = new ComboBox<>("Role");

        private final ComboBox<String> activeField = new ComboBox<>("Status");

        private final TextField addressLineField = new TextField("Address Line");

        private final TextField streetField = new TextField("Street");

        private final TextField cityField = new TextField("City");

        private final TextField stateField = new TextField("State");

        private final TextField countryField = new TextField("Country");

        private final TextField postalCodeField = new TextField("Pincode");

        private Employee employee;

        public EmployeeEditView(EmployeeService employeeService, DepartmentService departmentService,
                        RoleService roleService, SecurityService securityService) {

                this.employeeService = employeeService;
                this.securityService = securityService;

                setSizeFull();
                setPadding(true);

                employeeNameField.setRequired(true);
                employeeNameField.setPattern("^(?=.{3,72}$)[A-Za-z]+(?:[ '.][A-Za-z]+)*$");
                employeeNameField.setMaxLength(72);
                employeeNameField.setErrorMessage(
                                "Enter a valid name. Only letters, spaces, apostrophe and dot are allowed.");

                employeeEmailField.setRequired(true);
                employeeEmailField.setMaxLength(100);
                employeeEmailField.setErrorMessage("Enter a valid email");

                phoneField.setPattern("^\\+?[0-9]{4,15}$");
                phoneField.setMaxLength(16);
                phoneField.setErrorMessage(
                                "Enter a valid phone number with 4 to 15 digits");

                postalCodeField.setPattern("[0-9a-zA-Z]{3,10}");
                postalCodeField.setMaxLength(10);
                postalCodeField.setErrorMessage(
                                "Enter a valid postal code (3-10 characters)");

                countryField.setPattern("^(?=.{2,50}$)[A-Za-z]+(?:\\s[A-Za-z]+)*$");
                countryField.setMaxLength(50);
                countryField.setErrorMessage("Enter a valid country name");

                stateField.setPattern("^(?=.{2,100}$)[A-Za-z]+(?:\\s[A-Za-z]+)*$");
                stateField.setMaxLength(100);
                stateField.setErrorMessage("Enter a valid state name");

                cityField.setPattern("^(?=.{2,150}$)[A-Za-z]+(?:\\s[A-Za-z]+)*$");
                cityField.setMaxLength(150);
                cityField.setErrorMessage("Enter a valid city name");

                departmentField.setRequired(true);
                roleField.setRequired(true);
                activeField.setRequired(true);

                departmentField.setItems(departmentService.getDepartments());

                departmentField.setItemLabelGenerator(Department::getDepartmentName);

                departmentField.setReadOnly(!securityService.canAccessView("employee-form"));

                roleField.setItems(roleService.getRoles());

                roleField.setReadOnly(!securityService.canAccessView("employee-form"));

                roleField.setItemLabelGenerator(Role::getRoleName);

                activeField.setItems("Active", "Inactive");

                activeField.setReadOnly(!securityService.canAccessView("employee-form"));

                employeeEmailField.setReadOnly(true);
        }

        @Override
        public void setParameter(BeforeEvent event, Long employeeId) {

                removeAll();
                try{
                if (securityService.getLoggedInUser().getEmployee().getEmployeeId().equals(employeeId) || securityService.canAccessView("management-group")) {
                        employee = employeeService.getEmployeeById(employeeId).get();
                } else {
                        event.forwardTo("");
                        event.getUI().access(() -> {

                                Notification.show("Access Denied", 3000, Notification.Position.MIDDLE);
                        });
                }

                if (employee == null) {

                        add(new H2("Employee Not Found"));
                        return;
                }

                H2 title = new H2("Update Employee");

                employeeNameField.setValue( employee.getEmployeeName() == null? "" : employee.getEmployeeName());

                employeeEmailField.setValue( employee.getEmployeeEmail() == null? "" : employee.getEmployeeEmail());

                phoneField.setValue( employee.getEmployeePhoneNumber() == null? "": employee.getEmployeePhoneNumber());

                departmentField.setValue(employee.getDepartment());

                roleField.setValue( employee.getRole());

                activeField.setValue( employee.getActive() != null && employee.getActive()? "Active" : "Inactive");

                Address address = employee.getAddress();

                if (address != null) {

                        addressLineField.setValue(address.getAddressLine() == null ? "" : address.getAddressLine());
                        streetField.setValue(address.getStreet() == null ? "" : address.getStreet());
                        cityField.setValue(address.getCity() == null ? "" : address.getCity());
                        stateField.setValue(address.getState() == null ? "" : address.getState());
                        countryField.setValue(address.getCountry() == null ? "" : address.getCountry());
                        postalCodeField.setValue(address.getPostalCode() == null ? "" : address.getPostalCode());
                }

                FormLayout formLayout = new FormLayout();

                formLayout.add( employeeNameField, 
                                employeeEmailField,
                                phoneField,
                                departmentField,
                                roleField,
                                activeField,
                                addressLineField,
                                streetField,
                                cityField,
                                stateField,
                                countryField,
                                postalCodeField);

                formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

                Button saveButton = new Button("Save");

                saveButton.addClickListener(clickEvent -> {

                        try {
                                if (employeeNameField.isInvalid() || employeeEmailField.isInvalid() || phoneField.isInvalid() || postalCodeField.isInvalid()
                                                || cityField.isInvalid() || stateField.isInvalid() || countryField.isInvalid()) {
                                        Notification.show("Please correct validation errors",3000, Notification.Position.MIDDLE);
                                        return;
                                }
                                employee.setEmployeeName(employeeNameField.getValue());

                                employee.setEmployeeEmail(employeeEmailField.getValue());

                                String str = phoneField.getValue().trim().equals("") ? null
                                                : phoneField.getValue().trim();
                                employee.setEmployeePhoneNumber(str);

                                employee.setDepartment(departmentField.getValue());

                                employee.setRole(roleField.getValue());

                                employee.setActive(
                                                activeField.getValue().equals("Active"));

                                Address updatedAddress = employee.getAddress();

                                if (updatedAddress == null) {
                                        updatedAddress = new Address();
                                }

                                updatedAddress.setAddressLine(addressLineField.getValue());
                                updatedAddress.setStreet(streetField.getValue());
                                updatedAddress.setCity(cityField.getValue());
                                updatedAddress.setState(stateField.getValue());
                                updatedAddress.setCountry(countryField.getValue());
                                updatedAddress.setPostalCode(postalCodeField.getValue());

                                employee.setAddress(updatedAddress);
                                employee = employeeService.updateEmployee(employee,
                                                securityService.getLoggedInUser().getEmployee());
                                Notification.show("Employee Updated Successfully", 3000,
                                                Notification.Position.TOP_CENTER);
                                getUI().ifPresent(ui -> ui.navigate("employee-details/" + employee.getEmployeeId()));

                        } catch (Exception exception) {
                                Notification.show(exception.getMessage(), 5000, Notification.Position.TOP_CENTER);
                        }
                });
                Button cancelButton = new Button("Cancel");
                cancelButton.addClickListener(clickEvent -> {
                        getUI().ifPresent(ui -> ui.navigate("employee-details/" + employee.getEmployeeId()));
                });
                HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
                add(title, formLayout, buttonLayout);
        }catch(Exception ex){ 
                event.forwardTo("employee");
                event.getUI().access(() -> {Notification.show(ex.getMessage(),3000,Notification.Position.TOP_CENTER);});
                return;
        }    
        }
}