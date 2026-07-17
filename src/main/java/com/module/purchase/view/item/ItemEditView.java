package com.module.purchase.view.item;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.Unit;
import com.module.purchase.enums.ViewName;
import com.module.purchase.service.CategoryService;
import com.module.purchase.service.ItemService;
import com.module.purchase.service.UnitService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "item-edit", layout = MainLayout.class)
@PermitAll
public class ItemEditView extends VerticalLayout implements HasUrlParameter<String> {

    private final ItemService itemService;
    private final SecurityService securityService;

    private final TextField itemNameField = new TextField("Item Name");
    private final TextField itemCodeField = new TextField("Item Code");

    private final ComboBox<Category> categoryField =
            new ComboBox<>("Category");

    private final ComboBox<Unit> unitField =
            new ComboBox<>("Unit");

    private Item item;

    public ItemEditView(
            ItemService itemService,
            CategoryService categoryService,
            UnitService unitService,
            SecurityService securityService) {

        this.itemService = itemService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);

        itemNameField.setRequired(true);
        itemCodeField.setRequired(true);
        itemCodeField.setReadOnly(true);

        categoryField.setItems(categoryService.getCategories());
        categoryField.setItemLabelGenerator(Category::getCategoryName);

        unitField.setItems(unitService.getAllUnits());
        unitField.setItemLabelGenerator(Unit::getName);
    }

    @Override
    public void setParameter(BeforeEvent event, String itemId) {

        removeAll();
        try{
        item = itemService.getItemById(Long.parseLong(itemId)).orElse(null);

        if (item == null) {
            add(new H2("Item Not Found"));
            return;
        }

        H2 title = new H2("Update Item");

        itemNameField.setValue(item.getItemName() == null ? "" : item.getItemName());

        itemCodeField.setValue( item.getItemCode() == null ? "" : item.getItemCode());

        categoryField.setValue(item.getCategory());
        unitField.setValue(item.getUnit());

        FormLayout formLayout = new FormLayout();

        formLayout.add( itemNameField,itemCodeField,categoryField, unitField);

        formLayout.setResponsiveSteps( new FormLayout.ResponsiveStep("0", 2));

        Button saveButton = new Button("Save");

        saveButton.addClickListener(e -> {

            try {

                if (itemNameField.isEmpty()|| itemCodeField.isEmpty()|| categoryField.isEmpty() || unitField.isEmpty()) {
                    Notification.show("Please fill all required fields",3000,Notification.Position.TOP_CENTER);
                    return;
                }

                item.setItemName(itemNameField.getValue());
                item.setItemCode(itemCodeField.getValue());
                item.setCategory(categoryField.getValue());
                item.setUnit(unitField.getValue());

                itemService.updateItem(item,securityService.getLoggedInUser().getEmployee());

                Notification.show( "Item Updated Successfully",3000, Notification.Position.TOP_CENTER);

                getUI().ifPresent(ui -> ui.navigate("item-details/" + item.getItemId()));

            } catch (Exception ex) {

                Notification.show(ex.getMessage(),5000, Notification.Position.TOP_CENTER);
            }
        });

        Button cancelButton = new Button("Cancel");

        cancelButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("item-details/" + item.getItemId()))
        );

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_ERROR);
        
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        add(title, formLayout, buttons);
        }catch (NumberFormatException e) {
            event.forwardTo(ViewName.ITEM.getRoute());
            event.getUI().access(() -> {
                Notification.show("url is not valid ," + e.getMessage(), 3000,
                        Notification.Position.TOP_CENTER);
            });
            return;
        }catch(Exception ex){ 
            event.forwardTo(ViewName.ITEM.getRoute());
            event.getUI().access(() -> {Notification.show(ex.getMessage(),3000,Notification.Position.TOP_CENTER);});
            return;
        }
    }
}