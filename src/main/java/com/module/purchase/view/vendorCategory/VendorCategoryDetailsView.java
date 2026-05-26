package com.module.purchase.view.vendorCategory;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.VendorCategory;
import com.module.purchase.service.VendorCategoryService;
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

@Route(value = "vendor-category-details", layout = MainLayout.class)
@PermitAll
public class VendorCategoryDetailsView extends VerticalLayout
                implements HasUrlParameter<Long> {

        private final VendorCategoryService vendorCategoryService;

        private final SecurityService securityService;

        public VendorCategoryDetailsView(VendorCategoryService vendorCategoryService,SecurityService securityService) {
                this.vendorCategoryService = vendorCategoryService;
                this.securityService=securityService;

                setSizeFull();
                setPadding(true);
                setSpacing(true);
        }

        @Override
        public void setParameter(BeforeEvent event, Long categoryId) {

                removeAll();

                VendorCategory category = vendorCategoryService.getVendorCategoryById(categoryId).orElse(null);

                if (category == null) {
                        add(new Span("Vendor Category Not Found"));
                        return;
                }

                H2 title = new H2("Vendor Category Details");

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

                        dialog.setHeader("Delete Vendor Category");
                        dialog.setText("Are you sure you want to delete this category?");

                        dialog.setCancelable(true);
                        dialog.setConfirmText("Delete");
                        dialog.setConfirmButtonTheme("error primary");

                        dialog.addConfirmListener(confirmEvent -> {

                                try {
                                        vendorCategoryService.deleteVendorCategoryById(category.getCategoryId(),securityService.getLoggedInUser().getEmployee());

                                        Notification.show(
                                                        "Vendor Category Deleted Successfully",
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