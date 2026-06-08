package com.module.purchase.view.assigningConfig;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.ViewName;
import com.module.purchase.service.AssigningConfigService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "assigning-config-edit", layout = MainLayout.class)
@PermitAll
public class AssigningConfigEditView extends VerticalLayout
                implements HasUrlParameter<Long> {

        private final AssigningConfigService assigningConfigService;

        private final SecurityService securityService;

        private final ComboBox<ApprovalType> approvalTypeField = new ComboBox<>("Approval Type");

        private final IntegerField levelField = new IntegerField("Level");

        private final ComboBox<EmployeeGroup> employeeGroupField = new ComboBox<>("Role Group");

        private final NumberField minAmountField = new NumberField("Min Amount");

        private final NumberField maxAmountField = new NumberField("Max Amount");

        private final NumberField marginDifferencePercentageField = new NumberField("Margin Difference (%)");

        private AssigningConfig assigningConfig;

        public AssigningConfigEditView(
                        AssigningConfigService assigningConfigService,
                        SecurityService securityService) {

                this.assigningConfigService = assigningConfigService;
                this.securityService = securityService;

                setSizeFull();
                setPadding(true);

                approvalTypeField.setItems(ApprovalType.values());

                employeeGroupField.setItems(EmployeeGroup.getApprovalGroups());
                employeeGroupField.setItemLabelGenerator(EmployeeGroup::getDisplayName);
        }

        @Override
        public void setParameter(BeforeEvent event, Long assigningConfigId) {

                removeAll();

                assigningConfig = assigningConfigService
                                .getAssigningConfigById(assigningConfigId)
                                .orElse(null);

                if (assigningConfig == null) {

                        add(new H2("Assigning Config Not Found"));
                        return;
                }

                H2 title = new H2("Update Assigning Config");

                // SET VALUES
                approvalTypeField.setValue(
                                assigningConfig.getApprovalType());

                levelField.setValue(
                                assigningConfig.getLevel());

                employeeGroupField.setValue(
                                assigningConfig.getEmployeeGroup());

                minAmountField.setValue(
                                assigningConfig.getMinAmount());

                maxAmountField.setValue(
                                assigningConfig.getMaxAmount());

                marginDifferencePercentageField.setValue(
                                assigningConfig.getMarginDifferencePercentage());

                FormLayout formLayout = new FormLayout();

                formLayout.add(
                                approvalTypeField,
                                levelField,
                                employeeGroupField,
                                minAmountField,
                                maxAmountField,
                                marginDifferencePercentageField);

                formLayout.setResponsiveSteps(
                                new FormLayout.ResponsiveStep("0", 2));

                Button saveButton = new Button("Save");

                saveButton.addClickListener(clickEvent -> {

                        try {

                                if (approvalTypeField.isEmpty()
                                                || levelField.isEmpty()
                                                || employeeGroupField.isEmpty()
                                                || minAmountField.isEmpty()
                                                || maxAmountField.isEmpty()
                                                || marginDifferencePercentageField.isEmpty()) {

                                        Notification.show(
                                                        "Please fill all required fields",
                                                        3000,
                                                        Notification.Position.TOP_CENTER);

                                        return;
                                }

                                if (levelField.getValue() < 1) {

                                        levelField.setInvalid(true);
                                        levelField.setErrorMessage(
                                                        "Level must be greater than 0");

                                        return;
                                }

                                if (minAmountField.getValue() < 0) {

                                        minAmountField.setInvalid(true);
                                        minAmountField.setErrorMessage(
                                                        "Minimum amount cannot be negative");

                                        return;
                                }

                                if (maxAmountField.getValue() != null
                                                && maxAmountField.getValue() <= minAmountField.getValue()) {

                                        maxAmountField.setInvalid(true);
                                        maxAmountField.setErrorMessage(
                                                        "Maximum amount must be greater than minimum amount");

                                        return;
                                }

                                if (marginDifferencePercentageField.getValue() < 0) {

                                        marginDifferencePercentageField.setInvalid(true);

                                        marginDifferencePercentageField.setErrorMessage(
                                                        "Margin Difference cannot be negative");

                                        return;
                                }

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

                                assigningConfigService.updateAssigningConfig(
                                                assigningConfig,
                                                securityService
                                                                .getLoggedInUser()
                                                                .getEmployee());

                                Notification.show(
                                                "Assigning Config Updated Successfully",
                                                3000,
                                                Notification.Position.TOP_CENTER);

                                getUI().ifPresent(ui -> ui.navigate(
                                                ViewName.ASSIGNING_CONFIG_DETAILS.getRoute()
                                                                + "/"
                                                                + assigningConfig.getId()));

                        } catch (Exception exception) {

                                Notification.show(
                                                exception.getMessage(),
                                                5000,
                                                Notification.Position.TOP_CENTER);
                        }
                });

                Button cancelButton = new Button("Cancel");

                cancelButton.addClickListener(clickEvent -> getUI().ifPresent(ui -> ui.navigate(
                                ViewName.ASSIGNING_CONFIG_DETAILS.getRoute()
                                                + "/"
                                                + assigningConfig.getId())));

                HorizontalLayout buttonLayout = new HorizontalLayout(
                                saveButton,
                                cancelButton);

                add(
                                title,
                                formLayout,
                                buttonLayout);
        }
}