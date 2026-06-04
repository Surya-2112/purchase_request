package com.module.purchase.view.assigningConfig;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.entity.Employee;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.ViewName;
import com.module.purchase.service.AssigningConfigService;
import com.module.purchase.service.EmployeeService;
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

    private final EmployeeService employeeService;

    private final SecurityService securityService;

    private final ComboBox<ApprovalType> approvalTypeField =new ComboBox<>("Approval Type");

    private final IntegerField levelField =new IntegerField("Level");

    private final ComboBox<EmployeeGroup> employeeGroupField = new ComboBox<>("Role Group");

    private final NumberField minAmountField = new NumberField("Min Amount");

    private final NumberField maxAmountField = new NumberField("Max Amount");

    private final ComboBox<Employee> defaultEmployeeField =new ComboBox<>("Default Employee");

    private AssigningConfig assigningConfig;

    public AssigningConfigEditView(
            AssigningConfigService assigningConfigService,
            EmployeeService employeeService,
            SecurityService securityService) {

        this.assigningConfigService = assigningConfigService;
        this.employeeService = employeeService;
        this.securityService = securityService;

        setSizeFull();

        setPadding(true);

        approvalTypeField.setItems(ApprovalType.values());

        employeeGroupField.setItems( EmployeeGroup.values());

        defaultEmployeeField.setItemLabelGenerator( Employee::getEmployeeName);

        employeeGroupField.addValueChangeListener(event -> {

            EmployeeGroup group = event.getValue();

            if (group != null) {

                defaultEmployeeField.setItems(
                        employeeService
                                .getEmployeesByEmployeeGroup(group));

            } else {

                defaultEmployeeField.clear();

                defaultEmployeeField.setItems();
            }
        });
    }

    @Override
    public void setParameter(BeforeEvent event, Long assigningConfigId) {

        removeAll();

        assigningConfig =assigningConfigService.getAssigningConfigById(assigningConfigId).orElse(null);

        if (assigningConfig == null) {

            add(new H2("Assigning Config Not Found"));

            return;
        }

        H2 title =
                new H2("Update Assigning Config");

        // SET VALUES
        approvalTypeField.setValue(assigningConfig.getApprovalType());

        levelField.setValue(assigningConfig.getLevel());

        employeeGroupField.setValue(assigningConfig.getEmployeeGroup());

        minAmountField.setValue(assigningConfig.getMinAmount());

        maxAmountField.setValue(assigningConfig.getMaxAmount());

        if (assigningConfig.getEmployeeGroup() != null) {

            defaultEmployeeField.setItems(employeeService.getEmployeesByEmployeeGroup(assigningConfig.getEmployeeGroup()));
        }

        defaultEmployeeField.setValue( assigningConfig.getDefaultApprover());

        FormLayout formLayout = new FormLayout();

        formLayout.add(
                approvalTypeField,
                levelField,
                employeeGroupField,
                minAmountField,
                maxAmountField,
                defaultEmployeeField);

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2));

        // SAVE BUTTON
        Button saveButton = new Button("Save");

        saveButton.addClickListener(clickEvent -> {

            try {

                if (approvalTypeField.isEmpty()
                        || levelField.isEmpty()
                        || employeeGroupField.isEmpty()
                        || minAmountField.isEmpty()
                        || maxAmountField.isEmpty()
                        || defaultEmployeeField.isEmpty()) {

                    Notification.show(
                            "Please fill all required fields",
                            3000,
                            Notification.Position.TOP_CENTER);

                    return;
                }

                if (levelField.getValue() < 1) {

                    levelField.setInvalid(true);

                    levelField.setErrorMessage(
                        "Level must be higher than 0");

                    return;
                }

                if (minAmountField.getValue() < 1.0) {

                    minAmountField.setInvalid(true);

                    minAmountField.setErrorMessage(
                            "Minimum amount must be higher than 0");

                    return;
                }

                if (maxAmountField.getValue() < 1.0
                        || maxAmountField.getValue()
                                <= minAmountField.getValue()) {

                    maxAmountField.setInvalid(true);

                    maxAmountField.setErrorMessage(
                            "Maximum amount must be higher than minimum amount");

                    return;
                }

                // UPDATE VALUES
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

                assigningConfig.setDefaultApprover(
                        defaultEmployeeField.getValue());

                // UPDATE
                assigningConfigService.updateAssigningConfig(
                        assigningConfig,
                        securityService
                                .getLoggedInUser()
                                .getEmployee());

                Notification.show(
                        "Assigning Config Updated Successfully",
                        3000,
                        Notification.Position.TOP_CENTER);

                getUI().ifPresent(ui ->
                        ui.navigate(ViewName.ASSIGNING_CONFIG_DETAILS.getRoute()+"/"+ assigningConfig.getId()));

            } catch (Exception exception) {

                Notification.show(
                        exception.getMessage(),
                        5000,
                        Notification.Position.TOP_CENTER);
            }

        });
        
        Button cancelButton =
                new Button("Cancel");

        cancelButton.addClickListener(clickEvent -> {

            getUI().ifPresent(ui ->
                    ui.navigate(ViewName.ASSIGNING_CONFIG_DETAILS.getRoute()+"/"+ assigningConfig.getId()));

        });

        HorizontalLayout buttonLayout =
                new HorizontalLayout(
                        saveButton,
                        cancelButton);

        add(
                title,
                formLayout,
                buttonLayout);
    }
}