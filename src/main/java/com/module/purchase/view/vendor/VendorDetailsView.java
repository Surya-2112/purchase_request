package com.module.purchase.view.vendor;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Vendor;
import com.module.purchase.service.VendorService;
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

@Route(value = "vendor-details", layout = MainLayout.class)
@PermitAll
public class VendorDetailsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final VendorService vendorService;

    private final SecurityService securityService;


    public VendorDetailsView(VendorService vendorService,SecurityService securityService) {

        this.vendorService = vendorService;
        this.securityService=securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent event, Long vendorId) {

        removeAll();

        Vendor vendor = vendorService.getVendorById(vendorId).orElse(null);

        if (vendor == null) {
            add(new Span("Vendor not found"));
            return;
        }

        H2 title = new H2("Vendor Details");

        FormLayout formLayout = new FormLayout();

        // BASIC DETAILS
        formLayout.addFormItem(
                new Span(String.valueOf(vendor.getVendorId())),
                "Vendor ID"
        );

        formLayout.addFormItem(
                new Span(vendor.getVendorName() == null ? "" : vendor.getVendorName()),
                "Vendor Name"
        );

        formLayout.addFormItem(
                new Span(vendor.getVendorEmail() == null ? "" : vendor.getVendorEmail()),
                "Email"
        );

        formLayout.addFormItem(
                new Span(vendor.getVendorPhoneNumber() == null ? "" : vendor.getVendorPhoneNumber()),
                "Phone"
        );

        // STATUS
        formLayout.addFormItem(
                new Span(
                        Boolean.TRUE.equals(vendor.getActive()) ? "Active" : "Inactive"
                ),
                "Status"
        );

        // ADDRESS
        if (vendor.getVendorAddress() != null) {

            formLayout.addFormItem(
                    new Span(vendor.getVendorAddress().getAddressLine()),
                    "Address Line"
            );

            formLayout.addFormItem(
                    new Span(vendor.getVendorAddress().getStreet()),
                    "Street"
            );

            formLayout.addFormItem(
                    new Span(vendor.getVendorAddress().getCity()),
                    "City"
            );

            formLayout.addFormItem(
                    new Span(vendor.getVendorAddress().getState()),
                    "State"
            );

            formLayout.addFormItem(
                    new Span(vendor.getVendorAddress().getCountry()),
                    "Country"
            );

            formLayout.addFormItem(
                    new Span(vendor.getVendorAddress().getPostalCode()),
                    "Pincode"
            );
        }

        // UPDATE BUTTON
        Button updateButton = new Button("Update");

        updateButton.addClickListener(e -> {
            getUI().ifPresent(ui ->
                    ui.navigate("vendor-edit/" + vendor.getVendorId())
            );
        });

        // DELETE BUTTON
        Button deleteButton = new Button("Delete");

        deleteButton.addClickListener(e -> {

            ConfirmDialog dialog = new ConfirmDialog();

            dialog.setHeader("Delete Vendor");
            dialog.setText("Are you sure you want to delete this vendor?");

            dialog.setCancelable(true);
            dialog.setConfirmText("Delete");
            dialog.setConfirmButtonTheme("error primary");

            dialog.addConfirmListener(confirmEvent -> {

                try {

                    vendorService.deleteVendorById(vendor.getVendorId(),securityService.getLoggedInUser().getEmployee());

                    Notification.show(
                            "Vendor Deleted Successfully",
                            3000,
                            Notification.Position.TOP_CENTER
                    );

                    getUI().ifPresent(ui -> ui.navigate("vendor"));

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

        updateButton.setVisible(securityService.canAccessView("vendor-edit"));
        deleteButton.setVisible(securityService.canAccessView("vendor-form"));

        HorizontalLayout buttons = new HorizontalLayout(updateButton, deleteButton);

        add(title, formLayout, buttons);
    }
}