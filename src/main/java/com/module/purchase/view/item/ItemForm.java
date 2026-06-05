package com.module.purchase.view.item;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Item;
import com.module.purchase.service.ItemService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;

public class ItemForm extends Dialog {

    private final ItemService itemService;

    private final SecurityService securityService;

    // FIELDS
    private final TextField itemNameField = new TextField("Item Name");

    private final TextField itemCodeField = new TextField("Item Code");

    private final NumberField unitAmountField= new NumberField("Unit Price");

    private final TextField VATCodeField= new TextField("VAT Code");

    public ItemForm(ItemService itemService,SecurityService securityService) {

        this.itemService = itemService;
        this.securityService=securityService;

        setHeaderTitle("Add Item");
        setWidth("600px");

        // REQUIRED
        itemNameField.setRequiredIndicatorVisible(true);
        itemCodeField.setRequiredIndicatorVisible(true);

        // FORM
        FormLayout formLayout = new FormLayout();

        formLayout.add(
                itemNameField,
                itemCodeField,
                unitAmountField,
                VATCodeField
        );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2)
        );

        // BUTTONS
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.addClickListener(e -> saveItem());
        cancelButton.addClickListener(e -> close());

        HorizontalLayout buttonLayout = new HorizontalLayout(
                saveButton,
                cancelButton
        );

        add(formLayout, buttonLayout);
    }

    private void saveItem() {

        try {
            if (itemNameField.isEmpty() || itemCodeField.isEmpty()) {

                Notification.show(
                        "Please fill all required fields",
                        3000,
                        Notification.Position.TOP_CENTER
                );

                return;
            }

            Item item = new Item();

            item.setItemName(itemNameField.getValue());
            item.setItemCode(itemCodeField.getValue());

            itemService.addItem(item,securityService.getLoggedInUser().getEmployee());

            Notification.show(
                    "Item Saved Successfully",
                    3000,
                    Notification.Position.TOP_CENTER
            );

            close();

        } catch (Exception e) {

            Notification.show(
                    "Error: " + e.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
            );
        }
    }
}