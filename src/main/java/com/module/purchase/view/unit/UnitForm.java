package com.module.purchase.view.unit;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Unit;
import com.module.purchase.service.UnitService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class UnitForm extends Dialog {

    private final UnitService unitService;
    private final SecurityService securityService;

    private Unit unit;

    // FIELDS
    private final TextField unitNameField = new TextField("Unit Name");
    private final TextField unitCodeField = new TextField("Unit Code");

    public UnitForm(UnitService unitService, SecurityService securityService) {

        this.unitService = unitService;
        this.securityService = securityService;

        setWidth("500px");

        H2 title = new H2("Add Unit");

        unitNameField.setRequired(true);
        unitCodeField.setRequired(true);

        FormLayout formLayout = new FormLayout();
        formLayout.add(unitNameField, unitCodeField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );

        Button saveButton = new Button("Save", e -> saveUnit());
        Button cancelButton = new Button("Cancel", e -> close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        add(title, formLayout, buttons);
    }

    // ===== EDIT MODE =====
    public void setUnit(Unit unit) {

        this.unit = unit;

        if (unit != null) {
            unitNameField.setValue(unit.getName() == null ? "" : unit.getName());
            unitCodeField.setValue(unit.getCode() == null ? "" : unit.getCode());
        }
    }

    // ===== SAVE =====
    private void saveUnit() {

        try {

            if (unitNameField.isEmpty() || unitCodeField.isEmpty()) {
                Notification.show(
                        "Please fill all required fields",
                        3000,
                        Notification.Position.TOP_CENTER
                );
                return;
            }

            if (unit == null) {
                unit = new Unit();
            }

            unit.setName(unitNameField.getValue().trim());
            unit.setCode(unitCodeField.getValue().trim());

            if (unit.getId() == null) {

                unitService.addUnit(
                        unit,
                        securityService.getLoggedInUser().getEmployee()
                );

                Notification.show(
                        "Unit Created Successfully",
                        3000,
                        Notification.Position.TOP_CENTER
                );

            } else {

                unitService.updateUnit(
                        unit,
                        securityService.getLoggedInUser().getEmployee()
                );

                Notification.show(
                        "Unit Updated Successfully",
                        3000,
                        Notification.Position.TOP_CENTER
                );
            }

            close();

        } catch (Exception ex) {

            Notification.show(
                    ex.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
            );
        }
    }
}