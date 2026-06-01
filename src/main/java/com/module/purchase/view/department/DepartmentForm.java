package com.module.purchase.view.department;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Department;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.EmployeeService;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class DepartmentForm extends Dialog {

    private final DepartmentService departmentService;
    private final SecurityService securityService;

    private final TextField departmentNameField = new TextField("Department Name");
    private final TextField departmentCodeField = new TextField("Department Code");
  

    public DepartmentForm(DepartmentService departmentService, EmployeeService employeeServices, SecurityService securityService) {
        this.departmentService = departmentService;
      //  this.employeeService = employeeServices;
        this.securityService = securityService;

     //   List<Employee> employees = employeeService.getEmployees();

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

            if(departmentCodeField.getValue().length()<4)
            {  
                departmentCodeField.setInvalid(true);
                departmentCodeField.setErrorMessage("Department code must be higher then 3");
            }

            Department department = new Department();

            department.setDepartmentName(departmentNameField.getValue());
            department.setDepartmentCode(departmentCodeField.getValue());
            department.setActive(true);
            departmentService.addDepartment(department,securityService.getLoggedInUser().getEmployee());
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
