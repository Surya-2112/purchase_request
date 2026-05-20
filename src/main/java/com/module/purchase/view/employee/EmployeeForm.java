package com.module.purchase.view.employee;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Address;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Role;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.RoleService;
import com.module.purchase.view.department.DepartmentForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
// import com.vaadin.ui.Alignment;

public class EmployeeForm extends Dialog {

        private final EmployeeService employeeService;

        private final SecurityService securityService;

        // BASIC DETAILS
        private final TextField employeeNameField = new TextField("Employee Name");

        private final EmailField employeeEmailField = new EmailField("Employee Email");

        private final TextField phoneNumberField = new TextField("Phone Number");

        // DEPARTMENT & ROLE
        private final ComboBox<Department> departmentField = new ComboBox<>("Department");

        private final ComboBox<Role> roleField = new ComboBox<>("Role");

        // ADDRESS
        private final TextField addressLineField = new TextField("Address Line");

        private final TextField streetField = new TextField("Street");

        private final TextField cityField = new TextField("City");

        private final TextField stateField = new TextField("State");

        private final TextField countryField = new TextField("Country");

        private final TextField postalCodeField = new TextField("Pincode");

        public EmployeeForm( EmployeeService employeeService,
                        DepartmentService departmentService,
                        RoleService roleService,
                        SecurityService securityService ) {

                this.employeeService = employeeService;
                this.securityService=securityService;

                setHeaderTitle("Add Employee");

                setWidth("700px");

                // LOAD DEPARTMENTS
                departmentField.setItems(
                                departmentService.getDepartments());

                departmentField.setItemLabelGenerator(
                                Department::getDepartmentName);

                // LOAD ROLES
                roleField.setItems(
                                roleService.getRoles());

                roleField.setItemLabelGenerator(
                                Role::getRoleName);

                // ADD MASTER BUTTONS
                Button addDepartmentButton = new Button("+");

                Button addRoleButton = new Button("+");

                addDepartmentButton.addClickListener(event -> {
                        DepartmentForm form = new DepartmentForm(departmentService, employeeService,securityService);
                        form.open();
                });

                addRoleButton.addClickListener(event -> {
                        getUI().ifPresent(ui -> ui.navigate("role"));
                });

                // REQUIRED FIELDS
                employeeNameField.setRequired(true);
                employeeNameField.setRequiredIndicatorVisible(true);

                employeeEmailField.setRequired(true);
                employeeEmailField.setRequiredIndicatorVisible(true);

                departmentField.setRequired(true);
                departmentField.setRequiredIndicatorVisible(true);

                roleField.setRequired(true);
                roleField.setRequiredIndicatorVisible(true);

                HorizontalLayout departmentLayout = new HorizontalLayout();
                departmentLayout.setWidthFull();
                departmentLayout.setAlignItems(FlexComponent.Alignment.END);
                departmentField.setWidthFull();

                departmentLayout.add(
                                departmentField,
                                addDepartmentButton);
                departmentLayout.expand(departmentField);

                HorizontalLayout roleLayout = new HorizontalLayout();

                roleLayout.setWidthFull();
                roleLayout.setAlignItems(FlexComponent.Alignment.END);
                roleField.setWidthFull();
                roleLayout.add(
                                roleField,
                                addRoleButton);
                roleLayout.expand(roleField);

                // FORM LAYOUT
                FormLayout formLayout = new FormLayout();

                formLayout.add(
                                employeeNameField,
                                employeeEmailField,
                                phoneNumberField,
                                departmentLayout,
                                roleLayout,
                                addressLineField,
                                streetField,
                                cityField,
                                stateField,
                                countryField,
                                postalCodeField);

                formLayout.setResponsiveSteps(
                                new FormLayout.ResponsiveStep("0", 2));

                // BUTTONS
                Button saveButton = new Button("Save");

                Button cancelButton = new Button("Cancel");

                saveButton.addClickListener(event -> saveEmployee());

                cancelButton.addClickListener(event -> close());

                HorizontalLayout buttonLayout = new HorizontalLayout(
                                saveButton,
                                cancelButton);

                add(formLayout, buttonLayout);
        }

        private void saveEmployee() {

                try {

                        Employee employee = new Employee();

                        employee.setEmployeeName(
                                        employeeNameField.getValue());

                        employee.setEmployeeEmail(
                                        employeeEmailField.getValue());

                        employee.setEmployeePhoneNumber(
                                        phoneNumberField.getValue());

                        employee.setDepartment(
                                        departmentField.getValue());

                        employee.setRole(
                                        roleField.getValue());

                        employee.setActive(true);

                        // ADDRESS
                        Address address = new Address();

                        address.setAddressLine(
                                        addressLineField.getValue());
                        address.setStreet(
                                        streetField.getValue());

                        address.setCity(
                                        cityField.getValue());

                        address.setState(
                                        stateField.getValue());

                        address.setCountry(
                                        countryField.getValue());

                        address.setPostalCode(
                                        postalCodeField.getValue());
                        if (employeeNameField.isEmpty()
                                        || employeeEmailField.isEmpty()
                                        || departmentField.isEmpty()
                                        || roleField.isEmpty()) {

                                Notification.show(
                                                "Please fill all required fields");

                                return;
                        }

                        employee.setAddress(address);

                        Employee create=securityService
                            .getLoggedInUser()
                            .getEmployee();
                        employeeService.addEmployee(employee,create);

                        Notification.show(
                                        "Employee Saved Successfully",
                                        3000,
                                        Notification.Position.TOP_CENTER);

                        close();

                } catch (Exception exception) {

                        Notification.show(
                                        "Error : " + exception.getMessage(),
                                        5000,
                                        Notification.Position.TOP_CENTER);
                }
        }
}
