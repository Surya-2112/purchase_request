package com.module.purchase.view.itemvariant;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.ItemVariant;
import com.module.purchase.service.ItemVariantService;
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

@Route(value = "item-variant-details", layout = MainLayout.class)
@PermitAll
public class ItemVariantDetailsView extends VerticalLayout
        implements HasUrlParameter<Long> {

    private final ItemVariantService itemVariantService;
    private final SecurityService securityService;

    public ItemVariantDetailsView(
            ItemVariantService itemVariantService,
            SecurityService securityService) {

        this.itemVariantService = itemVariantService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent event, Long variantId) {

        removeAll();

        ItemVariant variant =
                itemVariantService.getItemVariantById(variantId)
                        .orElse(null);

        if (variant == null) {

            add(new Span("Item Variant Not Found"));
            return;
        }

        H2 title = new H2("Item Variant Details");

        FormLayout formLayout = new FormLayout();

        // ID
        formLayout.addFormItem(
                new Span(String.valueOf(variant.getId())),
                "Variant ID");

        // ITEM
        formLayout.addFormItem(
                new Span(
                        variant.getItem() == null
                                ? ""
                                : variant.getItem().getItemName()),
                "Item");

        // SPECIFICATION
        formLayout.addFormItem(
                new Span(
                        variant.getSpecification() == null
                                ? ""
                                : variant.getSpecification()),
                "Specification");

        // ESTIMATED PRICE
        formLayout.addFormItem(
                new Span(
                        variant.getEstimatedUnitPrice() == null
                                ? ""
                                : variant.getEstimatedUnitPrice().toString()),
                "Estimated Unit Price");

        // ACTIVE
        formLayout.addFormItem(
                new Span(
                        Boolean.TRUE.equals(variant.getActive())
                                ? "Active"
                                : "Inactive"),
                "Status");

        // UPDATE BUTTON
        Button updateButton = new Button("Update");

        updateButton.addClickListener(e ->
                getUI().ifPresent(ui ->
                        ui.navigate(
                                "item-variant-edit/"
                                        + variant.getId()))
        );

        // DELETE BUTTON
        Button deleteButton = new Button("Delete");

        deleteButton.addClickListener(e -> {

            ConfirmDialog dialog = new ConfirmDialog();

            dialog.setHeader("Delete Item Variant");

            dialog.setText(
                    "Are you sure you want to delete this Item Variant?");

            dialog.setCancelable(true);

            dialog.setConfirmText("Delete");

            dialog.setConfirmButtonTheme("error primary");

            dialog.addConfirmListener(confirmEvent -> {

                try {

                    itemVariantService.deleteItemVariantById(
                            variant.getId(),
                            securityService.getLoggedInUser()
                                    .getEmployee());

                    Notification.show(
                            "Item Variant Deleted Successfully",
                            3000,
                            Notification.Position.TOP_CENTER);

                    getUI().ifPresent(ui ->
                            ui.navigate("item-variant"));

                } catch (Exception ex) {

                    Notification.show(
                            ex.getMessage(),
                            5000,
                            Notification.Position.TOP_CENTER);
                }
            });

            dialog.open();
        });

        updateButton.setVisible(
                securityService.canAccessView(
                        "item-variant-edit"));

        deleteButton.setVisible(
                securityService.canAccessView(
                        "item-variant-form"));

        HorizontalLayout buttons =
                new HorizontalLayout(
                        updateButton,
                        deleteButton);

        add(title, formLayout, buttons);
    }
}