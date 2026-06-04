package com.module.purchase.view.Category;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
import com.module.purchase.service.CategoryService;
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

@Route(value = "category-details", layout = MainLayout.class)
@PermitAll
public class CategoryDetailsView extends VerticalLayout
                implements HasUrlParameter<Long> {

        private final CategoryService categoryService;

        private final SecurityService securityService;

        public CategoryDetailsView(CategoryService categoryService,SecurityService securityService) {
                this.categoryService = categoryService;
                this.securityService=securityService;

                setSizeFull();
                setPadding(true);
                setSpacing(true);
        }

        @Override
        public void setParameter(BeforeEvent event, Long categoryId) {

                removeAll();

                Category category = categoryService.getCategoryById(categoryId).orElse(null);

                if (category == null) {
                        add(new Span("Category Not Found"));
                        return;
                }

                H2 title = new H2("Category Details");

                FormLayout formLayout = new FormLayout();

                formLayout.addFormItem(new Span(String.valueOf(category.getCategoryId())),
                                "Category ID");

                formLayout.addFormItem(new Span(category.getCategoryName() == null ? "" : category.getCategoryName()),
                                "Category Name");

                // UPDATE BUTTON
                Button updateButton = new Button("Update");

                updateButton.addClickListener(e -> getUI()
                                .ifPresent(ui -> ui.navigate("vendor-category-edit/" + category.getCategoryId())));

                // DELETE BUTTON
                Button deleteButton = new Button("Delete");

                deleteButton.addClickListener(e -> {

                        ConfirmDialog dialog = new ConfirmDialog();

                        dialog.setHeader("Delete Category");
                        dialog.setText("Are you sure you want to delete this category?");

                        dialog.setCancelable(true);
                        dialog.setConfirmText("Delete");
                        dialog.setConfirmButtonTheme("error primary");

                        dialog.addConfirmListener(confirmEvent -> {

                                try {
                                        categoryService.deleteCategoryById(category.getCategoryId(),securityService.getLoggedInUser().getEmployee());

                                        Notification.show(
                                                        "Category Deleted Successfully",
                                                        3000,
                                                        Notification.Position.TOP_CENTER);

                                        getUI().ifPresent(ui -> ui.navigate("vendor-category"));

                                } catch (Exception exception) {

                                        Notification.show(
                                                        exception.getMessage(),
                                                        5000,
                                                        Notification.Position.TOP_CENTER);
                                }
                        });

                        dialog.open();
                });
                updateButton.setVisible(securityService.canAccessView("vendor-category-edit"));
                deleteButton.setVisible(securityService.canAccessView("vendor-category-form"));

                HorizontalLayout buttons = new HorizontalLayout(updateButton, deleteButton);

                add(title, formLayout, buttons);
        }
}