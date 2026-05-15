package com.module.purchase.view.vendorCategory;

import com.module.purchase.entity.VendorCategory;
import com.module.purchase.service.VendorCategoryService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class VendorCategoryForm extends Dialog {

    private final VendorCategoryService vendorCategoryService;

    private final TextField categoryNameField = new TextField("Category Name");

    private VendorCategory category;

    public VendorCategoryForm(VendorCategoryService vendorCategoryService) {

        this.vendorCategoryService = vendorCategoryService;

        setHeaderTitle("Add Vendor Category");
        setWidth("500px");

        categoryNameField.setRequiredIndicatorVisible(true);

        FormLayout formLayout = new FormLayout();
        formLayout.add(categoryNameField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.addClickListener(e -> saveCategory());
        cancelButton.addClickListener(e -> close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        add(formLayout, buttons);
    }

    // OPTIONAL: for EDIT mode reuse
    public void setCategory(VendorCategory category) {
        this.category = category;

        if (category != null) {
            categoryNameField.setValue(
                    category.getCategoryName() == null ? "" : category.getCategoryName()
            );

            setHeaderTitle("Update Vendor Category");
        }
    }

    private void saveCategory() {

        try {

            if (categoryNameField.isEmpty()) {
                Notification.show(
                        "Please enter category name",
                        3000,
                        Notification.Position.TOP_CENTER
                );
                return;
            }

            if (category == null) {
                category = new VendorCategory();
            }

            category.setCategoryName(categoryNameField.getValue());

            vendorCategoryService.addVendorCategory(category);

            Notification.show(
                    "Vendor Category Saved Successfully",
                    3000,
                    Notification.Position.TOP_CENTER
            );

            close();

        } catch (Exception exception) {

            Notification.show(
                    "Error: " + exception.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
            );
        }
    }
}