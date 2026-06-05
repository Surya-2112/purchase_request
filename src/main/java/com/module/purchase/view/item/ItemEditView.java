package com.module.purchase.view.item;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Item;
import com.module.purchase.service.ItemService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "item-edit", layout = MainLayout.class)
@PermitAll
public class ItemEditView extends VerticalLayout implements HasUrlParameter<Long> {

    private final ItemService itemService;

    private final SecurityService securityService;

    // FIELDS
    private final TextField itemNameField = new TextField("Item Name");
    private final TextField itemCodeField = new TextField("Item Code");
    private final NumberField unitPriceField = new NumberField("Unit Price");
    private final TextField VATCodeField = new TextField("VAT code");

    private Item item;

    public ItemEditView(ItemService itemService, SecurityService securityService) {

        this.itemService = itemService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);

        itemNameField.setRequired(true);
        itemCodeField.setRequired(true);
        itemCodeField.setReadOnly(true);
    }

    @Override
    public void setParameter(BeforeEvent event, Long itemId) {

        removeAll();

        item = itemService.getItemById(itemId).orElse(null);

        if (item == null) {
            add(new H2("Item Not Found"));
            return;
        }

        H2 title = new H2("Update Item");

        // SET VALUES
        itemNameField.setValue( item.getItemName() == null ? "" : item.getItemName());

        itemCodeField.setValue(item.getItemCode() == null ? "" : item.getItemCode() );
        

        // FORM
        FormLayout formLayout = new FormLayout();

        formLayout.add(
                itemNameField,
                itemCodeField,
                unitPriceField,
                VATCodeField
        );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2)
        );

        // SAVE BUTTON
        Button saveButton = new Button("Save");

        saveButton.addClickListener(e -> {

            try {

                // VALIDATION
                if (itemNameField.isEmpty() || itemCodeField.isEmpty()) {

                    Notification.show(
                            "Please fill all required fields",
                            3000,
                            Notification.Position.TOP_CENTER
                    );

                    return;
                }

                // UPDATE VALUES
                item.setItemName(itemNameField.getValue());
                item.setItemCode(itemCodeField.getValue());

                // UPDATE
                itemService.updateItem(item,securityService.getLoggedInUser().getEmployee());

                Notification.show(
                        "Item Updated Successfully",
                        3000,
                        Notification.Position.TOP_CENTER
                );

                getUI().ifPresent(ui ->
                        ui.navigate("item-details/" + item.getItemId())
                );

            } catch (Exception ex) {

                Notification.show(
                        ex.getMessage(),
                        5000,
                        Notification.Position.TOP_CENTER
                );
            }
        });

        // CANCEL BUTTON
        Button cancelButton = new Button("Cancel");

        cancelButton.addClickListener(e ->
                getUI().ifPresent(ui ->
                        ui.navigate("item-details/" + item.getItemId())
                )
        );

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        add(title, formLayout, buttons);
    }
}