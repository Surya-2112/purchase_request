package com.module.purchase.view.unit;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Unit;
import com.module.purchase.service.UnitService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "unit-edit", layout = MainLayout.class)
@PermitAll
public class UnitEditView extends VerticalLayout implements HasUrlParameter<Integer> {

    private final UnitService unitService;
    private final SecurityService securityService;

    private Unit unit;

    private final TextField nameField = new TextField("Unit Name");
    private final TextField codeField = new TextField("Unit Code");

    public UnitEditView(UnitService unitService, SecurityService securityService) {

        this.unitService = unitService;
        this.securityService = securityService;

        nameField.setPattern("[a-zA-Z]{3,50}");
        nameField.setRequired(true);
        nameField.setMaxLength(50);
        nameField.setErrorMessage("Enter vaild Unit name.3 to 50 letters");
        codeField.setPattern("[a-zA-Z0-9]{1,10}");
        codeField.setRequired(true);
        codeField.setMaxLength(10);
        codeField.setErrorMessage("Enter vaild Unit Code.Maximum length is 10, numbers and letters");

        setSizeFull();
        setPadding(true);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer unitId) {

        removeAll();
        try{
        unit = unitService.getUnitById(unitId).orElse(null);

        if (unit == null) {
            add(new H2("Unit Not Found"));
            return;
        }

        H2 title = new H2("Update Unit");

        nameField.setValue(unit.getName() == null ? "": unit.getName());
        codeField.setValue(unit.getCode() == null? "": unit.getCode());
        FormLayout formLayout = new FormLayout();

        formLayout.add(nameField, codeField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        Button saveButton = new Button("Update");

        saveButton.addClickListener(e -> {

            try {
                if (nameField.isEmpty() || codeField.isEmpty()) {
                    Notification.show("Please fill all required fields",3000,Notification.Position.TOP_CENTER);
                    return;
                }

                if(nameField.isInvalid() || codeField.isInvalid())
                {
                   Notification.show("Please correct validation errors",3000,Notification.Position.TOP_CENTER);
                }

                unit.setName(nameField.getValue().trim());
                unit.setCode(codeField.getValue().trim());
                unitService.updateUnit(unit,securityService.getLoggedInUser().getEmployee());
                Notification.show("Unit Updated Successfully",3000,Notification.Position.TOP_CENTER);

                getUI().ifPresent(ui -> ui.navigate("unit-details/" + unit.getId()));

            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_CENTER );
            }
        });

        Button cancelButton = new Button("Cancel");

        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("unit-details/" + unit.getId())));

        HorizontalLayout buttons =new HorizontalLayout(saveButton, cancelButton);
        add(title, formLayout, buttons);

        }catch (Exception ex) {
            event.forwardTo("unit");
            event.getUI().access(() -> {Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);});
            return;
        }
    }
}