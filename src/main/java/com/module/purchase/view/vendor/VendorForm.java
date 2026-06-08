package com.module.purchase.view.vendor;

import java.util.ArrayList;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Address;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.Vendor;
import com.module.purchase.service.CategoryService;
import com.module.purchase.service.VendorService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

public class VendorForm extends Dialog {

    private final VendorService vendorService;
    private final CategoryService categoryService;
    private final SecurityService securityService;

    // BASIC DETAILS
    private final TextField vendorNameField = new TextField("Vendor Name");
    private final EmailField vendorEmailField = new EmailField("Vendor Email");
    private final TextField vendorPhoneField = new TextField("Vendor Phone");

    // CATEGORY
    private final MultiSelectComboBox<Category> categoryField =
            new MultiSelectComboBox<>("Categories");

    // ADDRESS
    private final TextField addressLineField = new TextField("Address Line");
    private final TextField streetField = new TextField("Street");
    private final TextField cityField = new TextField("City");
    private final TextField stateField = new TextField("State");
    private final TextField countryField = new TextField("Country");
    private final TextField postalCodeField = new TextField("Pincode");

    public VendorForm(
            VendorService vendorService,
            CategoryService categoryService,
            SecurityService securityService) {

        this.vendorService = vendorService;
        this.categoryService = categoryService;
        this.securityService = securityService;

        setHeaderTitle("Add Vendor");
        setWidth("700px");

        // Required Fields
        vendorNameField.setRequired(true);
        vendorEmailField.setRequired(true);

        // Categories
        categoryField.setItems(categoryService.getCategories());
        categoryField.setItemLabelGenerator(Category::getCategoryName);

        FormLayout formLayout = new FormLayout();

        formLayout.add(
                vendorNameField,
                vendorEmailField,
                vendorPhoneField,
                categoryField,
                addressLineField,
                streetField,
                cityField,
                stateField,
                countryField,
                postalCodeField
        );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2)
        );

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.addClickListener(e -> saveVendor());
        cancelButton.addClickListener(e -> close());

        HorizontalLayout buttons =
                new HorizontalLayout(saveButton, cancelButton);

        add(formLayout, buttons);
    }

    private void saveVendor() {

        try {

            if (vendorNameField.isEmpty()
                    || vendorEmailField.isEmpty()) {

                Notification.show(
                        "Please fill all required fields",
                        3000,
                        Notification.Position.TOP_CENTER
                );
                return;
            }

            Vendor vendor = new Vendor();

            // BASIC DETAILS
            vendor.setVendorName(
                    vendorNameField.getValue().trim());

            vendor.setVendorEmail(
                    vendorEmailField.getValue().trim());

            vendor.setVendorPhoneNumber(
                    vendorPhoneField.getValue().trim());

            vendor.setActive(true);

            // CATEGORIES
            vendor.setCategories(
                    new ArrayList<>(categoryField.getValue()));

            // ADDRESS
            Address address = new Address();

            address.setAddressLine(
                    addressLineField.getValue().trim());

            address.setStreet(
                    streetField.getValue().trim());

            address.setCity(
                    cityField.getValue().trim());

            address.setState(
                    stateField.getValue().trim());

            address.setCountry(
                    countryField.getValue().trim());

            address.setPostalCode(
                    postalCodeField.getValue().trim());

            vendor.setVendorAddress(address);

            // SAVE
            vendorService.addVendor(
                    vendor,
                    securityService.getLoggedInUser().getEmployee());

            Notification.show(
                    "Vendor Saved Successfully",
                    3000,
                    Notification.Position.TOP_CENTER
            );

            close();

        } catch (Exception ex) {

            Notification.show(
                    "Error: " + ex.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
            );
        }
    }
}