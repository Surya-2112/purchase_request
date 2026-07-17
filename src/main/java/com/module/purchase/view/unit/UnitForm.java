package com.module.purchase.view.unit;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Unit;
import com.module.purchase.service.UnitService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

    private final TextField unitNameField = new TextField("Unit Name");
    private final TextField unitCodeField = new TextField("Unit Code");

    public UnitForm(UnitService unitService, SecurityService securityService) {

        this.unitService = unitService;
        this.securityService = securityService;

        setWidth("500px");

        H2 title = new H2("Add Unit");

        unitNameField.setPattern("[a-zA-Z]{3,50}");
        unitNameField.setRequired(true);
        unitNameField.setMaxLength(50);
        unitNameField.setErrorMessage("Enter vaild Unit name.3 to 50 letters");
        unitCodeField.setPattern("[a-zA-Z0-9]{1,10}");
        unitCodeField.setRequired(true);
        unitCodeField.setMaxLength(10);
        unitCodeField.setErrorMessage("Enter vaild Unit Code.Maximum length is 10, numbers and letters");

        FormLayout formLayout = new FormLayout();
        formLayout.add(unitNameField, unitCodeField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1));

        Button saveButton = new Button("Save", e -> saveUnit());
        Button cancelButton = new Button("Cancel", e -> close());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_ERROR);

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        add(title, formLayout, buttons);
    }

    public void setUnit(Unit unit) {

        this.unit = unit;

        if (unit != null) {
            unitNameField.setValue(unit.getName() == null ? "" : unit.getName());
            unitCodeField.setValue(unit.getCode() == null ? "" : unit.getCode());
        }
    }

    private void saveUnit() {

        try {

            if (unitNameField.isEmpty() || unitCodeField.isEmpty()) {
                Notification.show("Please fill all required fields", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            if (unitNameField.isInvalid() || unitCodeField.isInvalid()) {
                Notification.show("Please correct validation errors", 3000, Notification.Position.TOP_CENTER);
            }

            if (unit == null) {
                unit = new Unit();
            }

            unit.setName(unitNameField.getValue().trim());
            unit.setCode(unitCodeField.getValue().trim());

            if (unit.getId() == null) {

                unitService.addUnit( unit, securityService.getLoggedInUser().getEmployee());

                Notification.show("Unit Created Successfully",3000, Notification.Position.TOP_CENTER);

            } else {

                unitService.updateUnit(  unit, securityService.getLoggedInUser().getEmployee());

                Notification.show( "Unit Updated Successfully", 3000, Notification.Position.TOP_CENTER);
            }

            close();

        } catch (Exception ex) {

            Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_CENTER);
        }
    }
}