package com.module.purchase.view.assigningConfig;

import com.module.purchase.config.SecurityService;
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
    private final SecurityService securityService;

    private final ComboBox<ApprovalType> approvalTypeField =
            new ComboBox<>("Approval Type");

    private final IntegerField levelField =
            new IntegerField("Approval Level");

    private final ComboBox<EmployeeGroup> employeeGroupField =
            new ComboBox<>("Employee Group");

    private final NumberField minAmountField =
            new NumberField("Minimum Amount");

    private final NumberField maxAmountField =
            new NumberField("Maximum Amount");

    private final NumberField marginDifferenceField =
            new NumberField("Margin Difference %");

    public AssigningConfigForm(
            AssigningConfigService assigningConfigService,
            SecurityService securityService) {

        this.assigningConfigService = assigningConfigService;
        this.securityService = securityService;

        setHeaderTitle("Add Assigning Configuration");
        setWidth("700px");

        // REQUIRED
        approvalTypeField.setRequiredIndicatorVisible(true);
        levelField.setRequiredIndicatorVisible(true);
        employeeGroupField.setRequiredIndicatorVisible(true);
        minAmountField.setRequiredIndicatorVisible(true);
        maxAmountField.setRequiredIndicatorVisible(true);
        marginDifferenceField.setRequiredIndicatorVisible(true);

        // DATA
        approvalTypeField.setItems(ApprovalType.values());
        employeeGroupField.setItems(EmployeeGroup.getApprovalGroups());
        employeeGroupField.setItemLabelGenerator(EmployeeGroup::getDisplayName);

        FormLayout formLayout = new FormLayout();

        formLayout.add(
                approvalTypeField,
                levelField,
                employeeGroupField,
                minAmountField,
                maxAmountField,
                marginDifferenceField
        );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2)
        );

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.addClickListener(event -> saveAssigningConfig());
        cancelButton.addClickListener(event -> close());

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

    private void saveAssigningConfig() {

        try {

            if (approvalTypeField.isEmpty()
                    || levelField.isEmpty()
                    || employeeGroupField.isEmpty()
                    || minAmountField.isEmpty()
                    || maxAmountField.isEmpty()
                    || marginDifferenceField.isEmpty()) {

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
                        "Level must be greater than 0"
                );

                return;
            }

            if (minAmountField.getValue() < 0) {

                minAmountField.setInvalid(true);
                minAmountField.setErrorMessage(
                        "Minimum amount cannot be negative"
                );

                return;
            }

            if (maxAmountField.getValue() <= minAmountField.getValue()) {

                maxAmountField.setInvalid(true);
                maxAmountField.setErrorMessage(
                        "Maximum amount must be greater than minimum amount"
                );

                return;
            }

            if (marginDifferenceField.getValue() < 0) {

                marginDifferenceField.setInvalid(true);
                marginDifferenceField.setErrorMessage(
                        "Margin difference cannot be negative"
                );

                return;
            }

            AssigningConfig assigningConfig =
                    new AssigningConfig();

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

            assigningConfig.setMarginDifferencePercentage(
                    marginDifferenceField.getValue());

            assigningConfigService.addAssigningConfig(
                    assigningConfig,
                    securityService
                            .getLoggedInUser()
                            .getEmployee()
            );

            Notification.show(
                    "Assigning Configuration Saved Successfully",
                    3000,
                    Notification.Position.TOP_CENTER
            );

            close();

        } catch (Exception exception) {

            Notification.show(
                    exception.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
            );
        }
    }
}