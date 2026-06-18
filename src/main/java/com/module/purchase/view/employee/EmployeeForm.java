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
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "employee-form", layout = MainLayout.class)
@PermitAll
public class EmployeeForm extends VerticalLayout {
   
    private final EmployeeService employeeService;
    private final SecurityService securityService;

    private final TextField employeeNameField = new TextField("Employee Name");

    private final EmailField employeeEmailField =new EmailField("Employee Email");

    private final TextField phoneNumberField = new TextField("Phone Number");

    private final ComboBox<Department> departmentField = new ComboBox<>("Department");

    private final ComboBox<Role> roleField =  new ComboBox<>("Role");

    private final TextField addressLineField = new TextField("Address Line");

    private final TextField streetField =  new TextField("Street");

    private final TextField cityField = new TextField("City");

    private final TextField stateField =new TextField("State");

    private final TextField countryField = new TextField("Country");

    private final TextField postalCodeField =  new TextField("Pincode");

    public EmployeeForm(EmployeeService employeeService,
            DepartmentService departmentService,
            RoleService roleService,
            SecurityService securityService) {

        this.employeeService = employeeService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Add Employee");

        departmentField.setItems( departmentService.getDepartments());

        departmentField.setItemLabelGenerator(
                Department::getDepartmentName);

        roleField.setItems( roleService.getRoles());

        roleField.setItemLabelGenerator(
                Role::getRoleName);

        phoneNumberField.setPattern("[0-9]{10}");
        phoneNumberField.setErrorMessage("Enter valid 10 digit number");
        postalCodeField.setPattern("[0-9]{6}");
        postalCodeField.setErrorMessage("Enter vaild 6 digit postal code");

        employeeNameField.setRequired(true);
        employeeEmailField.setRequired(true);
        departmentField.setRequired(true);
        roleField.setRequired(true);

        FormLayout formLayout = new FormLayout();

        formLayout.add(
                employeeNameField,
                employeeEmailField,
                phoneNumberField,
                departmentField,
                roleField,
                addressLineField,
                streetField,
                cityField,
                stateField,
                countryField,
                postalCodeField
        );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2)
        );

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.addClickListener(e -> saveEmployee());

        cancelButton.addClickListener(e ->
                getUI().ifPresent(ui ->
                        ui.navigate("employee"))
        );

        HorizontalLayout buttons =
                new HorizontalLayout(saveButton, cancelButton);

        add(title, formLayout, buttons);
    }

    private void saveEmployee() {

        try {

            if (employeeNameField.isEmpty()
                    || employeeEmailField.isEmpty()
                    || departmentField.isEmpty()
                    || roleField.isEmpty()) {

                Notification.show(
                        "Please fill all required fields"
                );

                return;
            }

            Employee employee = new Employee();

            employee.setEmployeeName(
                    employeeNameField.getValue());

            employee.setEmployeeEmail(employeeEmailField.getValue());

            employee.setEmployeePhoneNumber(phoneNumberField.getValue().trim().equals("")?null:phoneNumberField.getValue());

            employee.setDepartment(
                    departmentField.getValue());

            employee.setRole(
                    roleField.getValue());

            employee.setActive(true);

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

            employee.setAddress(address);

            Employee createdBy =
                    securityService
                            .getLoggedInUser()
                            .getEmployee();

            employeeService.addEmployee(
                    employee,
                    createdBy
            );

            Notification.show(
                    "Employee Saved Successfully",
                    3000,
                    Notification.Position.TOP_CENTER
            );

            getUI().ifPresent(ui ->
                    ui.navigate("employee"));

        } catch (Exception e) {

            Notification.show(
                    "Error : " + e.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
            );
        }
    }
}