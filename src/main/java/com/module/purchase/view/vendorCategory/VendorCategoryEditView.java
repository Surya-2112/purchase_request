package com.module.purchase.view.vendorCategory;

import com.module.purchase.entity.VendorCategory;
import com.module.purchase.service.VendorCategoryService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
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

@Route(value = "vendor-category-edit", layout = MainLayout.class)
@PermitAll
public class VendorCategoryEditView extends VerticalLayout
        implements HasUrlParameter<Long> {

    private final VendorCategoryService vendorCategoryService;

    private final TextField categoryNameField =
            new TextField("Category Name");

    private VendorCategory category;

    public VendorCategoryEditView(VendorCategoryService vendorCategoryService) {

        this.vendorCategoryService = vendorCategoryService;

        setSizeFull();
        setPadding(true);

        categoryNameField.setRequired(true);
        categoryNameField.setRequiredIndicatorVisible(true);
    }

    @Override
    public void setParameter(BeforeEvent event, Long categoryId) {

        removeAll();

        category = vendorCategoryService.getVendorCategoryById(categoryId).orElse(null);

        if (category == null) {
            add(new H2("Vendor Category Not Found"));
            return;
        }

        H2 title = new H2("Update Vendor Category");

        // SET VALUES
        categoryNameField.setValue(
                category.getCategoryName() == null
                        ? ""
                        : category.getCategoryName()
        );

        FormLayout formLayout = new FormLayout();
        formLayout.add(categoryNameField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );

        // SAVE BUTTON
        Button saveButton = new Button("Save");

        saveButton.addClickListener(e -> {

            try {

                if (categoryNameField.isEmpty()) {
                    Notification.show(
                            "Category Name is required",
                            3000,
                            Notification.Position.TOP_CENTER
                    );
                    return;
                }

                category.setCategoryName(categoryNameField.getValue());

                vendorCategoryService.updateVendorCategory(category);

                Notification.show(
                        "Vendor Category Updated Successfully",
                        3000,
                        Notification.Position.TOP_CENTER
                );

                getUI().ifPresent(ui ->
                        ui.navigate("vendor-category-details/" + category.getCategoryId())
                );

            } catch (Exception exception) {

                Notification.show(
                        exception.getMessage(),
                        5000,
                        Notification.Position.TOP_CENTER
                );
            }
        });

        // CANCEL BUTTON
        Button cancelButton = new Button("Cancel");

        cancelButton.addClickListener(e ->
                getUI().ifPresent(ui ->
                        ui.navigate("vendor-category-details/" + category.getCategoryId())
                )
        );

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        add(title, formLayout, buttons);
    }
}