package com.module.purchase.view.Category;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
import com.module.purchase.service.CategoryService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;


public class CategoryForm extends Dialog {

    private final CategoryService categoryService;

    private final SecurityService securityService;

    private final TextField categoryNameField = new TextField("Category Name");

    private Category category;

    public CategoryForm(CategoryService categoryService,SecurityService securityService) {

        this.categoryService = categoryService;
        this.securityService = securityService; 

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

    public void setCategory(Category category) {
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
                category = new Category();
            }

            category.setCategoryName(categoryNameField.getValue());

            categoryService.addCategory(category,securityService.getLoggedInUser().getEmployee());

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