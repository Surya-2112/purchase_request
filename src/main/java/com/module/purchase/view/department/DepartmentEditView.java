package com.module.purchase.view.department;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.enums.ViewName;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "department-edit", layout = MainLayout.class)
@PermitAll
public class DepartmentEditView extends VerticalLayout implements HasUrlParameter<String> {

    private final DepartmentService departmentService;
    private final SecurityService securityService;

    private final TextField departmentNameField = new TextField("Department Name");

    private final TextField departmentCodeField = new TextField("Department Code");

    private final ComboBox<Employee> departmentHeadField = new ComboBox<>("Department Head");

    private final RadioButtonGroup<String> activeField = new RadioButtonGroup<String>("Status");

    private Department department;

    public DepartmentEditView( DepartmentService departmentService, EmployeeService employeeService,SecurityService securityService) {

        this.departmentService = departmentService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);

        departmentHeadField.setItems(employeeService.getEmployees());

        departmentHeadField.setItemLabelGenerator( employee -> String.valueOf(employee.getEmployeeName()));

        activeField.setItems("Active","Inactive");
    }

    @Override
    public void setParameter(BeforeEvent event, String departmentId) {

        removeAll();

        try{
        department = departmentService.getDepartmentById(Long.parseLong(departmentId)).orElse(null);

        if (department == null) {

            add(new H2("Department Not Found"));

            return;
        }

        H2 title = new H2("Update Department");

        departmentNameField.setValue(department.getDepartmentName() == null ? "" : department.getDepartmentName());

        departmentCodeField.setValue(department.getDepartmentCode() == null ? "": department.getDepartmentCode());

        departmentCodeField.setReadOnly(true);

        departmentHeadField.setValue(department.getHeadEmployee());

        activeField.setValue(department.getActive() != null && department.getActive()? "Active": "Inactive");

        FormLayout formLayout = new FormLayout();

        formLayout.add(departmentNameField,departmentCodeField,departmentHeadField, activeField);
        formLayout.setResponsiveSteps( new FormLayout.ResponsiveStep("0", 2));

        Button saveButton = new Button("Save");

        saveButton.addClickListener(clickEvent -> {

            try {
                if (departmentNameField.isEmpty() || departmentCodeField.isEmpty()) {
                    Notification.show( "Please fill all required fields",3000,Notification.Position.TOP_CENTER);
                    return;
                }

             if(departmentCodeField.getValue().length()<4)
            {  
                departmentCodeField.setInvalid(true);
                departmentCodeField.setErrorMessage("Department code must be higher then 3");
            }

                department.setDepartmentName(departmentNameField.getValue());

                department.setDepartmentCode(departmentCodeField.getValue());

                department.setHeadEmployee(departmentHeadField.getValue());

                department.setActive( activeField.getValue() .equals("Active"));

                departmentService.updateDepartment(department,securityService.getLoggedInUser().getEmployee());

                Notification.show("Department Updated Successfully", 3000, Notification.Position.TOP_CENTER);

                getUI().ifPresent(ui ->ui.navigate("department-details/"+ department.getDepartmentId()));

            } catch (Exception exception) {
                Notification.show( exception.getMessage(), 5000, Notification.Position.TOP_CENTER);
            }

        });

        Button cancelButton = new Button("Cancel");

        cancelButton.addClickListener(clickEvent -> {
            getUI().ifPresent(ui -> ui.navigate("department-details/"+ department.getDepartmentId()));

        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_ERROR);

        HorizontalLayout buttonLayout =new HorizontalLayout(saveButton,cancelButton);

        add(title, formLayout, buttonLayout);
        }catch (NumberFormatException e) {
            event.forwardTo(ViewName.DEPARTMENT.getRoute());
            event.getUI().access(() -> {
                Notification.show("url is not valid ," + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
            });
            return;

        }catch(Exception ex)
        {       event.forwardTo("department");
                event.getUI().access(() -> {Notification.show(ex.getMessage(),3000,Notification.Position.TOP_CENTER);});
                return;
        }
    }
}