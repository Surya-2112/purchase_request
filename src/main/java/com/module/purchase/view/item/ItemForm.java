package com.module.purchase.view.item;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.ItemVariant;
import com.module.purchase.entity.Unit;
import com.module.purchase.service.CategoryService;
import com.module.purchase.service.ItemService;
import com.module.purchase.service.ItemVariantService;
import com.module.purchase.service.UnitService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;

public class ItemForm extends Dialog {

    private final ItemService itemService;
    private final ItemVariantService itemVariantService;
    private final SecurityService securityService;

    private final TextField itemNameField = new TextField("Item Name");
    private final TextField itemCodeField = new TextField("Item Code");
    private final ComboBox<Category> categoryField = new ComboBox<>("Category");
    private final ComboBox<Unit> unitField = new ComboBox<>("Unit");
    private final NumberField estimatedPriceField = new NumberField("Estimated Price");

    public ItemForm( ItemService itemService, ItemVariantService itemVariantService, CategoryService categoryService,
            UnitService unitService, SecurityService securityService) {

        this.itemService = itemService;
        this.itemVariantService = itemVariantService;
        this.securityService = securityService;

        setHeaderTitle("Add Item");
        setWidth("700px");

        itemNameField.setRequired(true);
        itemNameField.setPattern("^(?=.{2,100}$)[A-Za-z0-9]+(?:[ A-Za-z0-9'.-])*?$");
        itemNameField.setMaxLength(100);
        itemNameField.setErrorMessage("Enter a vaild Item name");

        itemCodeField.setPattern("^[A-Za-z0-9-]{4,20}$");
        itemCodeField.setMaxLength(20);
        itemCodeField.setErrorMessage("Enter a valid item code (4-20 characters, letters, numbers and hyphen only)");
        itemCodeField.setRequired(true);
        
        categoryField.setItems(categoryService.getCategories());
        categoryField.setItemLabelGenerator(Category::getCategoryName);
        categoryField.setRequired(true);

        unitField.setItems(unitService.getAllUnits());
        unitField.setItemLabelGenerator(Unit::getName);
        unitField.setRequired(true);

        estimatedPriceField.setRequired(true);
        estimatedPriceField.setMin(0.0);
        estimatedPriceField.setPlaceholder("0.00");

        FormLayout formLayout = new FormLayout();
        formLayout.add(itemNameField, itemCodeField, categoryField,unitField,estimatedPriceField);

        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.addClickListener(e -> saveItem());
        cancelButton.addClickListener(e -> close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        add(formLayout, buttons);
    }

    private void saveItem() {
        try {
            if (itemNameField.isEmpty() || itemCodeField.isEmpty() || categoryField.isEmpty()
                    || unitField.isEmpty() || estimatedPriceField.isEmpty()) {

                Notification.show("Please fill all required fields",3000,Notification.Position.TOP_CENTER);
                return;
            }

            if(itemNameField.isInvalid() || itemCodeField.isInvalid())
            {   Notification.show("Please correct validation errors",3000,Notification.Position.TOP_CENTER);
                return;
            }

            if (estimatedPriceField.getValue() < 0) {
                Notification.show("Estimated Price cannot be negative",3000,Notification.Position.TOP_CENTER);
                return;
            }

            Item item = new Item();
            item.setItemName(itemNameField.getValue().trim());
            item.setItemCode(itemCodeField.getValue().trim());
            item.setCategory(categoryField.getValue());
            item.setUnit(unitField.getValue());

            Item savedItem = itemService.addItem(item, securityService.getLoggedInUser().getEmployee());
            ItemVariant defaultVariant = new ItemVariant();
            defaultVariant.setItem(savedItem);
            defaultVariant.setSpecification("Default");
            defaultVariant.setEstimatedUnitPrice(estimatedPriceField.getValue());
            itemVariantService.addItemVariant(defaultVariant,securityService.getLoggedInUser().getEmployee());

            Notification.show( "Item and Default Variant Saved Successfully", 3000, Notification.Position.TOP_CENTER);
            close();

        } catch (Exception ex) {
            Notification.show(ex.getMessage(),5000, Notification.Position.TOP_CENTER);
        }
    }
}