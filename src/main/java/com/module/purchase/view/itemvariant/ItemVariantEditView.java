package com.module.purchase.view.itemvariant;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.ItemVariant;
import com.module.purchase.service.ItemService;
import com.module.purchase.service.ItemVariantService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "item-variant-edit", layout = MainLayout.class)
@PermitAll
public class ItemVariantEditView extends VerticalLayout implements HasUrlParameter<Long> {

    private final ItemVariantService itemVariantService;
    private final SecurityService securityService;
    private final ComboBox<Item> itemField = new ComboBox<>("Item");
    private final TextArea specificationField = new TextArea("Specification");
    private final NumberField estimatedPriceField = new NumberField("Estimated Unit Price");
    private final ComboBox<String> activeField = new ComboBox<>("Status");

    private ItemVariant itemVariant;

    public ItemVariantEditView(ItemVariantService itemVariantService,ItemService itemService, SecurityService securityService) {

        this.itemVariantService = itemVariantService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);

        itemField.setItems(itemService.getItems());
        itemField.setItemLabelGenerator(Item::getItemName);

        activeField.setItems("Active", "Inactive");
    }

    @Override
    public void setParameter(BeforeEvent event, Long variantId) {

        removeAll();

        try{
        itemVariant = itemVariantService.getItemVariantById(variantId).orElse(null);

        if (itemVariant == null) {

            add(new H2("Item Variant Not Found"));
            return;
        }

        H2 title = new H2("Update Item Variant");

        itemField.setValue(itemVariant.getItem());
        itemField.setReadOnly(true);

        specificationField.setValue( itemVariant.getSpecification() == null? "": itemVariant.getSpecification());
        specificationField.setReadOnly(true);

        if (itemVariant.getEstimatedUnitPrice() != null) {
            estimatedPriceField.setValue( itemVariant.getEstimatedUnitPrice());
        }
        activeField.setValue(Boolean.TRUE.equals(itemVariant.getActive())? "Active": "Inactive");
        if(itemVariantService.getItemVariantsByItem(itemVariant.getItem()).size()<2 && itemVariant.getActive())
        {  activeField.setReadOnly(true);}

        FormLayout formLayout = new FormLayout();

        formLayout.add(itemField, activeField, estimatedPriceField, specificationField);
        formLayout.setResponsiveSteps( new FormLayout.ResponsiveStep("0", 2));

        Button saveButton = new Button("Save");

        saveButton.addClickListener(e -> {

            try {
                if (itemField.isEmpty()) {
                    Notification.show("Please select an Item",3000,Notification.Position.TOP_CENTER);
                    return;
                }

                itemVariant.setItem( itemField.getValue());
                itemVariant.setSpecification(specificationField.getValue());
                itemVariant.setActive( activeField.getValue().equals("Active"));
                itemVariant.setEstimatedUnitPrice(estimatedPriceField.getValue());
                itemVariantService.updateItemVariant(itemVariant,securityService.getLoggedInUser().getEmployee());

                Notification.show("Item Variant Updated Successfully", 3000, Notification.Position.TOP_CENTER);

                getUI().ifPresent(ui ->ui.navigate("item-variant-details/"+ itemVariant.getId()));

            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 5000,Notification.Position.TOP_CENTER);
            }
        });

        Button cancelButton = new Button("Cancel");

        cancelButton.addClickListener(e ->getUI().ifPresent(ui ->ui.navigate( "item-variant-details/"+ itemVariant.getId())));

        HorizontalLayout buttons =new HorizontalLayout(saveButton,cancelButton);
        add(title, formLayout, buttons);
        }catch(Exception ex){ 
                event.forwardTo("item-variant");
                event.getUI().access(() -> {Notification.show(ex.getMessage(),3000,Notification.Position.TOP_CENTER);});
                return;
        }
    }
}