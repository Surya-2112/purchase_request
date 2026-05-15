package com.module.purchase.view.item;

import com.module.purchase.entity.Item;
import com.module.purchase.service.ItemService;
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

@Route(value = "item-details", layout = MainLayout.class)
@PermitAll
public class ItemDetailsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final ItemService itemService;

    public ItemDetailsView(ItemService itemService) {

        this.itemService = itemService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent event, Long itemId) {

        removeAll();

        Item item = itemService.getItemById(itemId).orElse(null);

        if (item == null) {
            add(new Span("Item Not Found"));
            return;
        }

        H2 title = new H2("Item Details");

        FormLayout formLayout = new FormLayout();

        // ITEM ID
        formLayout.addFormItem(
                new Span(String.valueOf(item.getItemId())),
                "Item ID"
        );

        // ITEM NAME
        formLayout.addFormItem(
                new Span(item.getItemName() == null ? "" : item.getItemName()),
                "Item Name"
        );

        // ITEM CODE
        formLayout.addFormItem(
                new Span(item.getItemCode() == null ? "" : item.getItemCode()),
                "Item Code"
        );

        // UPDATE BUTTON
        Button updateButton = new Button("Update");

        updateButton.addClickListener(e ->
                getUI().ifPresent(ui ->
                        ui.navigate("item-edit/" + item.getItemId())
                )
        );

        // DELETE BUTTON
        Button deleteButton = new Button("Delete");

        deleteButton.addClickListener(e -> {

            ConfirmDialog dialog = new ConfirmDialog();

            dialog.setHeader("Delete Item");
            dialog.setText("Are you sure you want to delete this item?");

            dialog.setCancelable(true);
            dialog.setConfirmText("Delete");
            dialog.setConfirmButtonTheme("error primary");

            dialog.addConfirmListener(confirmEvent -> {

                try {

                    itemService.deleteItemById(item.getItemId());

                    Notification.show(
                            "Item Deleted Successfully",
                            3000,
                            Notification.Position.TOP_CENTER
                    );

                    getUI().ifPresent(ui -> ui.navigate("item"));

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

        HorizontalLayout buttons = new HorizontalLayout(updateButton, deleteButton);

        add(title, formLayout, buttons);
    }
}
