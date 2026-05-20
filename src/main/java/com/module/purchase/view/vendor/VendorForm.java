package com.module.purchase.view.vendor;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Address;
import com.module.purchase.entity.Vendor;
import com.module.purchase.entity.VendorCategory;
import com.module.purchase.service.VendorCategoryService;
import com.module.purchase.service.VendorService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

public class VendorForm extends Dialog {

    private final VendorService vendorService;

    private final SecurityService securityService;

    // BASIC DETAILS
    private final TextField vendorNameField = new TextField("Vendor Name");
    private final EmailField vendorEmailField = new EmailField("Vendor Email");
    private final TextField vendorPhoneField = new TextField("Vendor Phone");

    // CATEGORY
    private final ComboBox<VendorCategory> vendorCategoryField =
            new ComboBox<>("Vendor Category");

    // ADDRESS
    private final TextField addressLineField = new TextField("Address Line");
    private final TextField streetField = new TextField("Street");
    private final TextField cityField = new TextField("City");
    private final TextField stateField = new TextField("State");
    private final TextField countryField = new TextField("Country");
    private final TextField postalCodeField = new TextField("Pincode");

    public VendorForm( VendorService vendorService,
            VendorCategoryService vendorCategoryService, SecurityService securityService) {

        this.vendorService = vendorService;
        this.securityService=securityService;

        setHeaderTitle("Add Vendor");
        setWidth("700px");

        // LOAD CATEGORY
        vendorCategoryField.setItems(vendorCategoryService.getVendorCategories());
        vendorCategoryField.setItemLabelGenerator(VendorCategory::getCategoryName);

        // REQUIRED
        vendorNameField.setRequired(true);
        vendorEmailField.setRequired(true);
        vendorCategoryField.setRequired(true);

        // FORM
        FormLayout formLayout = new FormLayout();

        formLayout.add(
                vendorNameField,
                vendorEmailField,
                vendorPhoneField,
                vendorCategoryField,
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

        // BUTTONS
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.addClickListener(e -> saveVendor());
        cancelButton.addClickListener(e -> close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        add(formLayout, buttons);
    }

    private void saveVendor() {

        try {

            // VALIDATION
            if (vendorNameField.isEmpty()
                    || vendorEmailField.isEmpty()
                    || vendorCategoryField.isEmpty()) {

                Notification.show(
                        "Please fill all required fields",
                        3000,
                        Notification.Position.TOP_CENTER
                );
                return;
            }

            Vendor vendor = new Vendor();

            // BASIC
            vendor.setVendorName(vendorNameField.getValue());
            vendor.setVendorEmail(vendorEmailField.getValue());
            vendor.setVendorPhone(vendorPhoneField.getValue());
            vendor.setVendorCategory(vendorCategoryField.getValue());
            vendor.setActive(true);

            // ADDRESS
            Address address = new Address();
            address.setAddressLine(addressLineField.getValue());
            address.setStreet(streetField.getValue());
            address.setCity(cityField.getValue());
            address.setState(stateField.getValue());
            address.setCountry(countryField.getValue());
            address.setPostalCode(postalCodeField.getValue());

            vendor.setVendorAddress(address);

            // SAVE
            vendorService.addVendor(vendor,securityService.getLoggedInUser().getEmployee());

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