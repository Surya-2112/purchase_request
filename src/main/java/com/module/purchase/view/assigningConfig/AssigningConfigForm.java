package com.module.purchase.view.assigningConfig;

import java.util.Collections;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.entity.Employee;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.service.AssigningConfigService;
import com.module.purchase.service.EmployeeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;

public class AssigningConfigForm extends Dialog {

    private final AssigningConfigService assigningConfigService;

    private final SecurityService securityService;

   // private final EmployeeService employeeService;

    // ================= FIELDS =================

    private final ComboBox<ApprovalType> approvalTypeField =
            new ComboBox<>("Approval Type");

    private final IntegerField levelField =
            new IntegerField("Level");

    private final ComboBox<EmployeeGroup> employeeGroupField =
            new ComboBox<>("Role Group");

    private final NumberField minAmountField =
            new NumberField("Min Amount");

    private final NumberField maxAmountField =
            new NumberField("Max Amount");

    private final ComboBox<Employee> defaultEmployeeField =
            new ComboBox<>("Default Employee");

    public AssigningConfigForm(

            AssigningConfigService assigningConfigService,

            EmployeeService employeeService,

            SecurityService securityService
    ) {

        this.assigningConfigService =
                assigningConfigService;

        //this.employeeService = employeeService;

        this.securityService =
                securityService;

        setHeaderTitle(
                "Add Assigning Config"
        );

        setWidth("700px");

        // ================= REQUIRED =================

        approvalTypeField.setRequired(true);

        approvalTypeField.setRequiredIndicatorVisible(true);

        levelField.setRequiredIndicatorVisible(true);

        employeeGroupField.setRequired(true);

        employeeGroupField.setRequiredIndicatorVisible(true);

        minAmountField.setRequiredIndicatorVisible(true);

        maxAmountField.setRequiredIndicatorVisible(true);

        defaultEmployeeField.setRequiredIndicatorVisible(true);

        // ================= LOAD ENUMS =================

        approvalTypeField.setItems(
                ApprovalType.values()
        );

        employeeGroupField.setItems(
                EmployeeGroup.values()
        );

        // ================= EMPLOYEE DROPDOWN =================

        defaultEmployeeField.setItemLabelGenerator(
                Employee::getEmployeeName
        );

        // LOAD EMPLOYEES BASED ON GROUP

        employeeGroupField.addValueChangeListener(event -> {

            EmployeeGroup selectedGroup =
                    event.getValue();

            if (selectedGroup != null) {

                defaultEmployeeField.setItems(
                        employeeService.getEmployeesByEmployeeGroup(selectedGroup));

            } else {

                defaultEmployeeField.clear();

                defaultEmployeeField.setItems(
                        Collections.emptyList()
                );
            }
        });

        // ================= FORM =================

        FormLayout formLayout =
                new FormLayout();

        formLayout.add(

                approvalTypeField,

                levelField,

                employeeGroupField,

                minAmountField,

                maxAmountField,

                defaultEmployeeField
        );

        formLayout.setResponsiveSteps(

                new FormLayout.ResponsiveStep(
                        "0",
                        2
                )
        );

        // ================= BUTTONS =================

        Button saveButton =
                new Button("Save");

        Button cancelButton =
                new Button("Cancel");

        saveButton.addClickListener(

                event -> saveAssigningConfig()
        );

        cancelButton.addClickListener(
                event -> close()
        );

        HorizontalLayout buttonLayout =
                new HorizontalLayout(

                        saveButton,

                        cancelButton
                );

        add(
                formLayout,
                buttonLayout
        );
    }

    // ================= SAVE =================

    private void saveAssigningConfig() {

        try {

            // ================= VALIDATION =================

            if (approvalTypeField.isEmpty()
                    || levelField.isEmpty()
                    || employeeGroupField.isEmpty()
                    || minAmountField.isEmpty()
                    || maxAmountField.isEmpty()
                    || defaultEmployeeField.isEmpty()) {

                Notification.show(

                        "Please fill all required fields",

                        3000,

                        Notification.Position.TOP_CENTER
                );

                return;
            }

            if (levelField.getValue() < 1) {

                levelField.setInvalid(true);

                levelField.setErrorMessage(
                        "Level must be higher than 0"
                );

                return;
            }

            if (minAmountField.getValue() < 1.0) {

                minAmountField.setInvalid(true);

                minAmountField.setErrorMessage(
                        "Minimum amount must be higher than 0"
                );

                return;
            }

            if (maxAmountField.getValue() < 1.0
                    || maxAmountField.getValue()
                            <= minAmountField.getValue()) {

                maxAmountField.setInvalid(true);

                maxAmountField.setErrorMessage(

                        "Maximum amount must be higher than minimum amount"
                );

                return;
            }

            // ================= CREATE ENTITY =================

            AssigningConfig assigningConfig =
                    new AssigningConfig();

            assigningConfig.setApprovalType(

                    approvalTypeField.getValue()
            );

            assigningConfig.setLevel(
                    levelField.getValue()
            );

            assigningConfig.setEmployeeGroup(

                    employeeGroupField.getValue()
            );

            assigningConfig.setMinAmount(

                    minAmountField.getValue()
            );

            assigningConfig.setMaxAmount(

                    maxAmountField.getValue()
            );

            assigningConfig.setDefaultApprover(

                    defaultEmployeeField.getValue()
            );

            // ================= SAVE =================

            assigningConfigService.addAssigningConfig(

                    assigningConfig,

                    securityService
                            .getLoggedInUser()
                            .getEmployee()
            );

            Notification.show(

                    "Assigning Config Saved Successfully",

                    3000,

                    Notification.Position.TOP_CENTER
            );

            close();

        } catch (Exception exception) {

            exception.printStackTrace();

            Notification.show( 
                "Error : "+ exception.getMessage(),5000,
                    Notification.Position.TOP_CENTER
            );
        }
    }
}