package com.module.purchase.view.department;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;

import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.EmployeeService;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class DepartmentForm extends Dialog {

    private DepartmentService departmentService;
    private EmployeeService employeeService;

    private final TextField departmentNameField = new TextField("Department Name");
    private final TextField departmentCodeField = new TextField("Department Code");
   // private final ComboBox<Employee> departmentHeadCombo = new ComboBox<>("Department Head");

    public DepartmentForm(DepartmentService departmentService, EmployeeService employeeService) {
        this.departmentService = departmentService;
        this.employeeService = employeeService;

        List<Employee> employees = employeeService.getEmployees();

        setHeaderTitle("Add Department");

        setWidth("700px");

        departmentNameField.setRequired(true);
        departmentNameField.setRequiredIndicatorVisible(true);

        departmentCodeField.setRequired(true);
        departmentCodeField.setRequiredIndicatorVisible(true);

        FormLayout formLayout = new FormLayout();
        formLayout.add(
                departmentNameField,
                departmentCodeField);

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2));

        // BUTTONS
        Button saveButton = new Button("Save");

        Button cancelButton = new Button("Cancel");

        saveButton.addClickListener(event -> saveDepartment());

        cancelButton.addClickListener(event -> close());

        HorizontalLayout buttonLayout = new HorizontalLayout(
                saveButton,
                cancelButton);

        add(formLayout, buttonLayout);
    }

    private void saveDepartment() {
        try {

            if (departmentNameField.isEmpty() || departmentCodeField.isEmpty()) {
                Notification.show(
                        "Please fill all required fields");
                return;
            }

            Department department = new Department();

            department.setDepartmentName(departmentNameField.getValue());
            department.setDepartmentCode(departmentCodeField.getValue());
            department.setActive(true);
            departmentService.addDepartment(department);
            Notification.show(
                    "Department Saved Successfully",
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
