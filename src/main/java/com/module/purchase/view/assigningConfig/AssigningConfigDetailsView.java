package com.module.purchase.view.assigningConfig;

import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.service.AssigningConfigService;
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

@Route(value = "assigning-config-details", layout = MainLayout.class)
@PermitAll
public class AssigningConfigDetailsView extends VerticalLayout
        implements HasUrlParameter<Long> {

    private final AssigningConfigService assigningConfigService;

    public AssigningConfigDetailsView(
            AssigningConfigService assigningConfigService) {

        this.assigningConfigService =
                assigningConfigService;

        setSizeFull();

        setPadding(true);

        setSpacing(true);
    }

    @Override
    public void setParameter(
            BeforeEvent event,
            Long assigningConfigId) {

        removeAll();

        AssigningConfig assigningConfig =
                assigningConfigService
                        .getAssigningConfigById(
                                assigningConfigId)
                        .orElse(null);

        if (assigningConfig == null) {

            add(new Span("Assigning Config Not Found"));

            return;
        }

        H2 title =
                new H2("Assigning Config Details");

        FormLayout formLayout =
                new FormLayout();

        // ID
        formLayout.addFormItem(
                new Span(
                        String.valueOf(
                                assigningConfig.getId())),
                "Config ID");

        // APPROVAL TYPE
        formLayout.addFormItem(
                new Span(
                        assigningConfig.getApprovalType() == null
                                ? ""
                                : assigningConfig
                                        .getApprovalType()
                                        .name()),
                "Approval Type");

        // LEVEL
        formLayout.addFormItem(
                new Span(
                        String.valueOf(
                                assigningConfig.getLevel())),
                "Level");

        // EMPLOYEE GROUP
        formLayout.addFormItem(
                new Span(
                        assigningConfig.getEmployeeGroup() == null
                                ? ""
                                : assigningConfig
                                        .getEmployeeGroup()
                                        .name()),
                "Employee Group");

        // MIN AMOUNT
        formLayout.addFormItem(
                new Span(
                        String.valueOf(
                                assigningConfig.getMinAmount())),
                "Min Amount");

        // MAX AMOUNT
        formLayout.addFormItem(
                new Span(
                        String.valueOf(
                                assigningConfig.getMaxAmount())),
                "Max Amount");

        // UPDATE BUTTON
        Button updateButton =
                new Button("Update");

        updateButton.addClickListener(clickEvent -> {

            getUI().ifPresent(ui ->
                    ui.navigate(
                            "assigning-config-edit/"
                                    + assigningConfig.getId()));
        });

        // DELETE BUTTON
        Button deleteButton =
                new Button("Delete");

        deleteButton.addClickListener(clickEvent -> {

            ConfirmDialog dialog =
                    new ConfirmDialog();

            dialog.setHeader(
                    "Delete Assigning Config");

            dialog.setText(
                    "Are you sure you want to delete this assigning config?");

            dialog.setCancelable(true);

            dialog.setConfirmText("Delete");

            dialog.setConfirmButtonTheme(
                    "error primary");

            dialog.addConfirmListener(confirmEvent -> {

                try {

                    assigningConfigService
                            .deleteAssigningConfigById(
                                    assigningConfig.getId());

                    Notification.show(
                            "Assigning Config Deleted Successfully");

                    getUI().ifPresent(ui ->
                            ui.navigate(
                                    "assigning-config"));

                } catch (Exception exception) {

                    Notification.show(
                            exception.getMessage(),
                            5000,
                            Notification.Position.TOP_CENTER);
                }

            });

            dialog.open();
        });

        HorizontalLayout buttonLayout =
                new HorizontalLayout(
                        updateButton,
                        deleteButton);

        add(
                title,
                formLayout,
                buttonLayout);
    }
}