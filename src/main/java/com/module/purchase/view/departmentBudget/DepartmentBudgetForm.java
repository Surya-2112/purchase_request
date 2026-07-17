package com.module.purchase.view.departmentBudget;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entityDTO.DepartmentDTO;
import com.module.purchase.service.DepartmentBudgetService;
import com.module.purchase.service.DepartmentService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;

public class DepartmentBudgetForm extends Dialog {

    private final DepartmentBudgetService departmentBudgetService;

    private final DepartmentService departmentService;

    private final SecurityService securityService;

    private final ComboBox<Department> departmentField = new ComboBox<>("Department");

    private final NumberField totalBudgetAmountField = new NumberField("Total Budget Amount");

    private final NumberField remainingBudgetAmountField = new NumberField("Remaining Budget Amount");

    private final ComboBox<Year> yearField = new ComboBox<>("Year");

    public DepartmentBudgetForm(DepartmentBudgetService departmentBudgetService, DepartmentService departmentServices,SecurityService securityService) {

        this.departmentBudgetService = departmentBudgetService;

        this.departmentService =  departmentServices;

        this.securityService=securityService;

        setHeaderTitle("Add Department Budget");

        setWidth("700px");

        departmentField.setRequired(true);

        totalBudgetAmountField.setRequiredIndicatorVisible(true);

        remainingBudgetAmountField.setRequiredIndicatorVisible(true);

        yearField.setRequired(true);

        DepartmentDTO departmentDTO= new DepartmentDTO();
        departmentDTO.setActive(true);
        List<Department> departments = departmentService.getAllDepartmentsList(departmentDTO);

        departmentField.setItems(departments);
        departmentField.setItemLabelGenerator(Department::getDepartmentName);

        List<Year> years = new ArrayList<>();

        for (int year = 2000; year <= 2100; year++) {

            years.add(Year.of(year));
        }

        yearField.setItems(years);
        yearField.setItemLabelGenerator(year -> String.valueOf(year.getValue()));

        FormLayout formLayout = new FormLayout();
        formLayout.add(departmentField, totalBudgetAmountField, remainingBudgetAmountField, yearField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        Button saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        Button cancelButton = new Button("Cancel");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_ERROR);
        
        saveButton.addClickListener( event -> saveDepartmentBudget());

        cancelButton.addClickListener( event -> close());

        HorizontalLayout buttonLayout = new HorizontalLayout( saveButton, cancelButton);

        add(formLayout, buttonLayout);
    }

    private void saveDepartmentBudget() {

        try {

            // VALIDATION
            if (departmentField.isEmpty()
                    || totalBudgetAmountField.isEmpty()
                    || remainingBudgetAmountField.isEmpty()
                    || yearField.isEmpty()) {

                Notification.show(
                        "Please fill all required fields",
                        3000,
                        Notification.Position.TOP_CENTER);
                return;
            }

            if(totalBudgetAmountField.getValue()<0)
            {
                totalBudgetAmountField.setEnabled(true);
                totalBudgetAmountField.setErrorMessage("Total budgets must be greater then 0");
            }

            if(remainingBudgetAmountField.getValue()<0 ||totalBudgetAmountField.getValue() >= remainingBudgetAmountField.getValue())
            {
                remainingBudgetAmountField.setEnabled(true);
                remainingBudgetAmountField.setErrorMessage("Remaining amount must be postive and smaller then Total amount");
            }

            DepartmentBudget departmentBudget =
                    new DepartmentBudget();

            // SET VALUES
            departmentBudget.setDepartment(
                    departmentField.getValue());

            departmentBudget.setTotalBudgetAmount(
                    totalBudgetAmountField.getValue());

            departmentBudget.setRemainingBudgetAmount(
                    remainingBudgetAmountField.getValue());

            departmentBudget.setYear(
                    yearField.getValue());

            // SAVE
            departmentBudgetService.addDepartmentBudget(departmentBudget,securityService.getLoggedInUser().getEmployee());

            Notification.show(
                    "Department Budget Saved Successfully",
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