package com.module.purchase.view.assigningConfig;

import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.service.AssigningConfigService;
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

    // FIELDS
    private final ComboBox<ApprovalType> approvalTypeField =
            new ComboBox<>("Approval Type");

    private final IntegerField levelField =
            new IntegerField("Level");

    private final ComboBox<EmployeeGroup> employeeGroupField =
            new ComboBox<>("Employee Group");

    private final NumberField minAmountField =
            new NumberField("Min Amount");

    private final NumberField maxAmountField =
            new NumberField("Max Amount");

    public AssigningConfigForm(AssigningConfigService assigningConfigService) {

        this.assigningConfigService = assigningConfigService;

        setHeaderTitle("Add Assigning Config");

        setWidth("700px");

        // REQUIRED
        approvalTypeField.setRequired(true);

        approvalTypeField.setRequiredIndicatorVisible(true);

        levelField.setRequiredIndicatorVisible(true);

        employeeGroupField.setRequired(true);

        employeeGroupField.setRequiredIndicatorVisible(true);

        minAmountField.setRequiredIndicatorVisible(true);

        maxAmountField.setRequiredIndicatorVisible(true);

        // LOAD ENUMS
        approvalTypeField.setItems(
                ApprovalType.values());

        employeeGroupField.setItems(
                EmployeeGroup.values());

        // FORM
        FormLayout formLayout =
                new FormLayout();

        formLayout.add(
                approvalTypeField,
                levelField,
                employeeGroupField,
                minAmountField,
                maxAmountField);

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2));

        // BUTTONS
        Button saveButton =
                new Button("Save");

        Button cancelButton =
                new Button("Cancel");

        saveButton.addClickListener(
                event -> saveAssigningConfig());

        cancelButton.addClickListener(
                event -> close());

        HorizontalLayout buttonLayout =
                new HorizontalLayout(
                        saveButton,
                        cancelButton);

        add(formLayout, buttonLayout);
    }

    private void saveAssigningConfig() {

        try {

            // VALIDATION
            if (approvalTypeField.isEmpty()
                    || levelField.isEmpty()
                    || employeeGroupField.isEmpty()
                    || minAmountField.isEmpty()
                    || maxAmountField.isEmpty()) {

                Notification.show(
                        "Please fill all required fields",
                        3000,
                        Notification.Position.TOP_CENTER);

                return;
            }

            AssigningConfig assigningConfig =
                    new AssigningConfig();

            // SET VALUES
            assigningConfig.setApprovalType(
                    approvalTypeField.getValue());

            assigningConfig.setLevel(
                    levelField.getValue());

            assigningConfig.setEmployeeGroup(
                    employeeGroupField.getValue());

            assigningConfig.setMinAmount(
                    minAmountField.getValue());

            assigningConfig.setMaxAmount(
                    maxAmountField.getValue());

            // SAVE
            assigningConfigService.addAssigningConfig(
                    assigningConfig);

            Notification.show(
                    "Assigning Config Saved Successfully",
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