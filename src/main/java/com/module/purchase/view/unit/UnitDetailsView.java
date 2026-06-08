package com.module.purchase.view.unit;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Unit;
import com.module.purchase.service.UnitService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "unit-details", layout = MainLayout.class)
@PermitAll
public class UnitDetailsView extends VerticalLayout
        implements HasUrlParameter<Integer> {

    private final UnitService unitService;
    private final SecurityService securityService;

    public UnitDetailsView(
            UnitService unitService,
            SecurityService securityService) {

        this.unitService = unitService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer unitId) {

        removeAll();

        Unit unit = unitService.getUnitById(unitId).orElse(null);

        if (unit == null) {
            add(new Span("Unit not found"));
            return;
        }

        H2 title = new H2("Unit Details");

        FormLayout formLayout = new FormLayout();

        formLayout.addFormItem(
                new Span(String.valueOf(unit.getId())),
                "Unit ID"
        );

        formLayout.addFormItem(
                new Span(unit.getName() == null ? "" : unit.getName()),
                "Unit Name"
        );

        formLayout.addFormItem(
                new Span(unit.getCode() == null ? "" : unit.getCode()),
                "Unit Code"
        );

        Button updateButton = new Button("Update");

        updateButton.addClickListener(e ->
                getUI().ifPresent(ui ->
                        ui.navigate("unit-edit/" + unit.getId()))
        );

        Button deleteButton = new Button("Delete");

        deleteButton.addClickListener(e -> {

            ConfirmDialog dialog = new ConfirmDialog();

            dialog.setHeader("Delete Unit");
            dialog.setText("Are you sure you want to delete this unit?");

            dialog.setCancelable(true);
            dialog.setConfirmText("Delete");
            dialog.setConfirmButtonTheme("error primary");

            dialog.addConfirmListener(confirmEvent -> {

                try {

                    unitService.deleteUnitById(
                            unit.getId(),
                            securityService.getLoggedInUser().getEmployee()
                    );

                    Notification.show(
                            "Unit deleted successfully",
                            3000,
                            Notification.Position.TOP_CENTER
                    );

                    getUI().ifPresent(ui -> ui.navigate("unit"));

                } catch (Exception ex) {

                    Notification.show(
                            ex.getMessage(),
                            5000,
                            Notification.Position.TOP_CENTER
                    );
                }
            });

            dialog.open();
        });

        updateButton.setVisible(
                securityService.canAccessView("unit-edit"));

        deleteButton.setVisible(
                securityService.canAccessView("unit-form"));

        HorizontalLayout buttons =
                new HorizontalLayout(updateButton, deleteButton);

        add(title, formLayout, buttons);
    }
}