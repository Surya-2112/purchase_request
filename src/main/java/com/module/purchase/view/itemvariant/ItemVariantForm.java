package com.module.purchase.view.itemvariant;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.ItemVariant;
import com.module.purchase.service.ItemService;
import com.module.purchase.service.ItemVariantService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;

public class ItemVariantForm extends Dialog {

    private final ItemVariantService itemVariantService;
    private final SecurityService securityService;

    private final ComboBox<Item> itemField = new ComboBox<>("Item");

    private final TextArea specificationField = new TextArea("Specification");

    private final NumberField estimatedPriceField = new NumberField("Estimated Unit Price");

    public ItemVariantForm(ItemVariantService itemVariantService, ItemService itemService, SecurityService securityService) {

        this.itemVariantService = itemVariantService;
        this.securityService = securityService;

        setHeaderTitle("Add Item Variant");
        setWidth("700px");

        itemField.setItems(itemService.getItems());
        itemField.setItemLabelGenerator(Item::getItemName);
        itemField.setRequired(true);

        specificationField.setWidthFull();
        specificationField.setMinHeight("120px");
        specificationField.setRequired(true);

        estimatedPriceField.setMin(0);

        FormLayout formLayout = new FormLayout();

        formLayout.add( itemField,estimatedPriceField,specificationField );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2)
        );

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        saveButton.addClickListener(e -> saveVariant());

        cancelButton.addClickListener(e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_ERROR);

        HorizontalLayout buttons =
                new HorizontalLayout(
                        saveButton,
                        cancelButton);

        add(formLayout, buttons);
    }

    private void saveVariant() {

        try {

            if (itemField.isEmpty()) {

                Notification.show(
                        "Please select an Item",
                        3000,
                        Notification.Position.TOP_CENTER);

                return;
            }

            ItemVariant variant =
                    new ItemVariant();

            variant.setItem(
                    itemField.getValue());

            variant.setSpecification(
                    specificationField.getValue());

            variant.setActive(true);

            if (estimatedPriceField.getValue() != null) {

                variant.setEstimatedUnitPrice(
                        estimatedPriceField.getValue());
            }

            itemVariantService.addItemVariant(
                    variant,
                    securityService.getLoggedInUser()
                            .getEmployee());

            Notification.show(
                    "Item Variant Saved Successfully",
                    3000,
                    Notification.Position.TOP_CENTER);

            close();

        } catch (Exception ex) {

            Notification.show(
                    ex.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER);
        }
    }
}