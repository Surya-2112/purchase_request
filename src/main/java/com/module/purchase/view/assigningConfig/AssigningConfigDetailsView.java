package com.module.purchase.view.assigningConfig;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.enums.ViewName;
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
public class AssigningConfigDetailsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final AssigningConfigService assigningConfigService;

    private final SecurityService securityService;

    public AssigningConfigDetailsView(AssigningConfigService assigningConfigService, SecurityService securityService) {

        this.assigningConfigService = assigningConfigService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void setParameter( BeforeEvent event,Long assigningConfigId) {

        removeAll();

        try{
        AssigningConfig assigningConfig =assigningConfigService.getAssigningConfigById(assigningConfigId).orElse(null);

        if (assigningConfig == null) {

            add(new Span("Assigning Config Not Found"));

            return;
        }

        H2 title = new H2("Assigning Config Details");

        FormLayout formLayout = new FormLayout();

        formLayout.setResponsiveSteps( new FormLayout.ResponsiveStep("0", 2));

        formLayout.addFormItem(new Span(String.valueOf(assigningConfig.getId())),"Config ID");
        formLayout.addFormItem(new Span(assigningConfig.getApprovalType() == null ? "": assigningConfig.getApprovalType().name()), "Approval Type");
        formLayout.addFormItem(new Span(assigningConfig.getLevel() == null? "": assigningConfig.getLevel().toString()),"Level");
        formLayout.addFormItem(new Span(assigningConfig.getEmployeeGroup() == null ? "": assigningConfig.getEmployeeGroup().name()), "Employee Group");
        formLayout.addFormItem(new Span(assigningConfig.getMinAmount() == null? "" : assigningConfig.getMinAmount().toString()),"Min Amount");
        formLayout.addFormItem(new Span(assigningConfig.getMaxAmount() == null? "": assigningConfig.getMaxAmount().toString()), "Max Amount");
        formLayout.addFormItem( new Span(assigningConfig.getMarginDifferencePercentage() == null ? "" : (assigningConfig.getMarginDifferencePercentage().toString()) +"%"),"Margin Difference");

        Button updateButton = new Button("Update");

        updateButton.addClickListener(clickEvent ->getUI().ifPresent(ui ->ui.navigate(ViewName.ASSIGNING_CONFIG_EDIT.getRoute()+ "/"+ assigningConfig.getId())));

        Button deleteButton = new Button("Delete");

        deleteButton.addClickListener(clickEvent -> {

            ConfirmDialog dialog = new ConfirmDialog();

            dialog.setHeader("Delete Assigning Config");

            dialog.setText("Are you sure you want to delete this assigning config?");

            dialog.setCancelable(true);

            dialog.setConfirmText("Delete");

            dialog.setConfirmButtonTheme("error primary");

            dialog.addConfirmListener(confirmEvent -> {

                try {
                    assigningConfigService.deleteAssigningConfigById( assigningConfig.getId(),securityService.getLoggedInUser().getEmployee());

                    Notification.show("Assigning Config Deleted Successfully",3000,Notification.Position.TOP_CENTER);

                    getUI().ifPresent(ui -> ui.navigate(ViewName.ASSIGNING_CONFIG.getRoute()));

                } catch (Exception exception) {
                    Notification.show(exception.getMessage(),5000,Notification.Position.TOP_CENTER);
                }
            });

            dialog.open();
        });

        updateButton.setVisible(securityService.canAccessView("assigning-config-edit"));

        deleteButton.setVisible(securityService.canAccessView("assigning-config-form"));

        HorizontalLayout buttonLayout =new HorizontalLayout(updateButton,deleteButton);

        add(title,formLayout, buttonLayout);

        }catch(Exception ex){      
                event.forwardTo("assigning-config");
                event.getUI().access(() -> {
                Notification.show(ex.getMessage(),3000,Notification.Position.TOP_CENTER);
        });
                return;
        }
    }
}