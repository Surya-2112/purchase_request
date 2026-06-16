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

        // BASIC DETAILS
        private final TextField employeeNameField = new TextField("Employee Name");

        private final EmailField employeeEmailField = new EmailField("Employee Email");

        private final TextField phoneField = new TextField("Phone Number");

        // DEPARTMENT & ROLE
        private final ComboBox<Department> departmentField = new ComboBox<>("Department");

        private final ComboBox<Role> roleField = new ComboBox<>("Role");

        // ACTIVE
        private final ComboBox<String> activeField = new ComboBox<>("Status");

        // ADDRESS
        private final TextField addressLineField = new TextField("Address Line");

        private final TextField streetField = new TextField("Street");

        private final TextField cityField = new TextField("City");

        private final TextField stateField = new TextField("State");

        private final TextField countryField = new TextField("Country");

        private final TextField postalCodeField = new TextField("Pincode");

        private Employee employee;

        public EmployeeEditView(EmployeeService employeeService,
                        DepartmentService departmentService,
                        RoleService roleService,
                        SecurityService securityService) {

                this.employeeService = employeeService;
                this.securityService = securityService;

                setSizeFull();
                setPadding(true);

                departmentField.setItems(departmentService.getDepartments());

                departmentField.setItemLabelGenerator(Department::getDepartmentName);

                departmentField.setReadOnly(!securityService.canAccessView("employee-form"));

                roleField.setItems(roleService.getRoles());

                roleField.setReadOnly(!securityService.canAccessView("employee-form"));

                roleField.setItemLabelGenerator(Role::getRoleName);

                // ACTIVE FIELD
                activeField.setItems("Active", "Inactive");

                activeField.setReadOnly(!securityService.canAccessView("employee-form"));

                employeeEmailField.setReadOnly(true);
        }

        @Override
        public void setParameter(BeforeEvent event, Long employeeId) {

                removeAll();

                employee = employeeService
                                .getEmployeeById(employeeId)
                                .orElse(null);

                if (employee == null) {

                        add(new H2("Employee Not Found"));
                        return;
                }

                H2 title = new H2("Update Employee");

                // SET VALUES
                employeeNameField.setValue(
                                employee.getEmployeeName() == null
                                                ? ""
                                                : employee.getEmployeeName());

                employeeEmailField.setValue(
                                employee.getEmployeeEmail() == null
                                                ? ""
                                                : employee.getEmployeeEmail());

                phoneField.setValue(
                                employee.getEmployeePhoneNumber() == null
                                                ? ""
                                                : employee.getEmployeePhoneNumber());

                departmentField.setValue(
                                employee.getDepartment());

                roleField.setValue(
                                employee.getRole());

                activeField.setValue(
                                employee.getActive() != null
                                                && employee.getActive()
                                                                ? "Active"
                                                                : "Inactive");

                // ADDRESS
                Address address = employee.getAddress();

                if (address != null) {

                        addressLineField.setValue(
                                        address.getAddressLine() == null
                                                        ? ""
                                                        : address.getAddressLine());

                        streetField.setValue(
                                        address.getStreet() == null
                                                        ? ""
                                                        : address.getStreet());

                        cityField.setValue(
                                        address.getCity() == null
                                                        ? ""
                                                        : address.getCity());

                        stateField.setValue(
                                        address.getState() == null
                                                        ? ""
                                                        : address.getState());

                        countryField.setValue(
                                        address.getCountry() == null
                                                        ? ""
                                                        : address.getCountry());

                        postalCodeField.setValue(
                                        address.getPostalCode() == null
                                                        ? ""
                                                        : address.getPostalCode());
                }

                // FORM
                FormLayout formLayout = new FormLayout();

                formLayout.add(
                                employeeNameField,
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

                formLayout.setResponsiveSteps(
                                new FormLayout.ResponsiveStep("0", 2));

                Button saveButton = new Button("Save");

                saveButton.addClickListener(clickEvent -> {

                        try {

                               phoneField.setPattern("[0-9]{10}");
                               phoneField.setErrorMessage("Enter valid 10 digit number");

                                postalCodeField.setPattern("[0-9]{6}");
                                postalCodeField.setErrorMessage("Enter vaild 6 digit postal code");

                                employee.setEmployeeName(
                                                employeeNameField.getValue());

                                employee.setEmployeeEmail(
                                                employeeEmailField.getValue());

                                String str=phoneField.getValue().equals("") ? null: phoneField.getValue();
                                System.out.println(str);
                                employee.setEmployeePhoneNumber(str);

                                employee.setDepartment(
                                                departmentField.getValue());

                                employee.setRole(
                                                roleField.getValue());

                                employee.setActive(
                                                activeField.getValue().equals("Active"));

                                Address updatedAddress = employee.getAddress();

                                if (updatedAddress == null) {
                                        updatedAddress = new Address();
                                }

                                updatedAddress.setAddressLine(
                                                addressLineField.getValue());

                                updatedAddress.setStreet(
                                                streetField.getValue());

                                updatedAddress.setCity(
                                                cityField.getValue());

                                updatedAddress.setState(
                                                stateField.getValue());

                                updatedAddress.setCountry(
                                                countryField.getValue());

                                updatedAddress.setPostalCode(
                                                postalCodeField.getValue());

                                employee.setAddress(updatedAddress);

                                employee=employeeService.updateEmployee(employee,securityService.getLoggedInUser().getEmployee());


                                Notification.show(
                                                "Employee Updated Successfully",
                                                3000,
                                                Notification.Position.TOP_CENTER);

                                getUI().ifPresent(ui -> ui.navigate(
                                                "employee-details/"
                                                                + employee.getEmployeeId()));

                        } catch (Exception exception) {

                                Notification.show(
                                                exception.getMessage(),
                                                5000,
                                                Notification.Position.TOP_CENTER);
                        }

                });
                Button cancelButton = new Button("Cancel");

                cancelButton.addClickListener(clickEvent -> {

                        getUI().ifPresent(ui -> ui.navigate(
                                        "employee-details/"
                                                        + employee.getEmployeeId()));

                });

                HorizontalLayout buttonLayout = new HorizontalLayout(
                                saveButton,
                                cancelButton);

                add(title, formLayout, buttonLayout);
        }
}