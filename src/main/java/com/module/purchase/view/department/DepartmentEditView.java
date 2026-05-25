package com.module.purchase.view.department;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "department-edit", layout = MainLayout.class)
@PermitAll
public class DepartmentEditView extends VerticalLayout
        implements HasUrlParameter<Long> {

    private final DepartmentService departmentService;

   // private final EmployeeService employeeService;

    private final SecurityService securityService;

    // FIELDS
    private final TextField departmentNameField = new TextField("Department Name");

    private final TextField departmentCodeField = new TextField("Department Code");

    private final ComboBox<Employee> departmentHeadField = new ComboBox<>("Department Head");

    private final ComboBox<String> activeField = new ComboBox<>("Status");

    private Department department;

    public DepartmentEditView( DepartmentService departmentService, EmployeeService employeeService,SecurityService securityService) {

        this.departmentService = departmentService;
      //  this.employeeService = employeeService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);

        // LOAD EMPLOYEES
        departmentHeadField.setItems(employeeService.getEmployees());

        departmentHeadField.setItemLabelGenerator(
                employee -> String.valueOf(employee.getEmployeeName()));

        // STATUS
        activeField.setItems(
                "Active",
                "Inactive");
    }

    @Override
    public void setParameter(
            BeforeEvent event,
            Long departmentId) {

        removeAll();

        department = departmentService
                .getDepartmentById(departmentId)
                .orElse(null);

        if (department == null) {

            add(new H2("Department Not Found"));

            return;
        }

        H2 title = new H2("Update Department");

        // SET VALUES
        departmentNameField.setValue(
                department.getDepartmentName() == null
                        ? ""
                        : department.getDepartmentName());

        departmentCodeField.setValue(
                department.getDepartmentCode() == null
                        ? ""
                        : department.getDepartmentCode());

        departmentCodeField.setReadOnly(true);

        departmentHeadField.setValue(department.getHeadEmployee());

        activeField.setValue(
                department.getActive() != null
                        && department.getActive()
                                ? "Active"
                                : "Inactive");

        // FORM
        FormLayout formLayout = new FormLayout();

        formLayout.add(
                departmentNameField,
                departmentCodeField,
                departmentHeadField,
                activeField);

        formLayout.setResponsiveSteps( new FormLayout.ResponsiveStep("0", 2));

        // SAVE BUTTON
        Button saveButton = new Button("Save");

        saveButton.addClickListener(clickEvent -> {

            try {

                // VALIDATION
                if (departmentNameField.isEmpty()
                        || departmentCodeField.isEmpty()) {

                    Notification.show(
                            "Please fill all required fields",
                            3000,
                            Notification.Position.TOP_CENTER);

                    return;
                }

             if(departmentCodeField.getValue().length()<4)
            {  
                departmentCodeField.setInvalid(true);
                departmentCodeField.setErrorMessage("Department code must be higher then 3");
            }

                // UPDATE VALUES
                department.setDepartmentName(
                        departmentNameField.getValue());

                department.setDepartmentCode(
                        departmentCodeField.getValue());

                department.setHeadEmployee(departmentHeadField.getValue());

                department.setActive(
                        activeField.getValue()
                                .equals("Active"));

                // SAVE
                departmentService.updateDepartment(department,securityService.getLoggedInUser().getEmployee());

                Notification.show(
                        "Department Updated Successfully",
                        3000,
                        Notification.Position.TOP_CENTER);

                getUI().ifPresent(ui ->
                        ui.navigate(
                                "department-details/"
                                        + department
                                                .getDepartmentId()));

            } catch (Exception exception) {

                Notification.show(
                        exception.getMessage(),
                        5000,
                        Notification.Position.TOP_CENTER);
            }

        });

        // CANCEL BUTTON
        Button cancelButton = new Button("Cancel");

        cancelButton.addClickListener(clickEvent -> {

            getUI().ifPresent(ui ->
                    ui.navigate(
                            "department-details/"
                                    + department
                                            .getDepartmentId()));

        });

        HorizontalLayout buttonLayout =
                new HorizontalLayout(
                        saveButton,
                        cancelButton);

        add(title, formLayout, buttonLayout);
    }
}