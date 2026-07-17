package com.module.purchase.view.vendor;

import java.util.ArrayList;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Address;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.Vendor;
import com.module.purchase.service.CategoryService;
import com.module.purchase.service.VendorService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

public class VendorForm extends Dialog {

    private final VendorService vendorService;
    private final SecurityService securityService;

    private final TextField vendorNameField = new TextField("Vendor Name");
    private final EmailField vendorEmailField = new EmailField("Vendor Email");
    private final TextField vendorPhoneField = new TextField("Vendor Phone");

    private final MultiSelectComboBox<Category> categoryField = new MultiSelectComboBox<>("Categories");

    private final TextField addressLineField = new TextField("Address Line");
    private final TextField streetField = new TextField("Street");
    private final TextField cityField = new TextField("City");
    private final TextField stateField = new TextField("State");
    private final TextField countryField = new TextField("Country");
    private final TextField postalCodeField = new TextField("Pincode");

    public VendorForm(VendorService vendorService, CategoryService categoryService, SecurityService securityService) {

        this.vendorService = vendorService;
        this.securityService = securityService;

        setHeaderTitle("Add Vendor");
        setWidth("700px");

        vendorNameField.setPattern("^(?=.{3,72}$)[A-Za-z]+(?:[ '.][A-Za-z]+)*$");
        vendorNameField.setMaxLength(72);
        vendorNameField.setErrorMessage("Enter a valid vendor name. Only letters, spaces, apostrophe and dot are allowed.");

        vendorEmailField.setRequired(true);
        vendorEmailField.setMaxLength(100);
        vendorEmailField.setErrorMessage("Enter a valid email");

        vendorPhoneField.setPattern("^\\+?[0-9]{4,15}$");
        vendorPhoneField.setMaxLength(16);
        vendorPhoneField.setErrorMessage( "Enter a valid phone number with 4 to 15 digits");

        postalCodeField.setPattern("[0-9a-zA-Z]{3,10}");
        postalCodeField.setMaxLength(10);
        postalCodeField.setErrorMessage( "Enter a valid postal code (3-10 characters)");

        countryField.setPattern("^(?=.{2,50}$)[A-Za-z]+(?:\\s[A-Za-z]+)*$");
        countryField.setMaxLength(50);
        countryField.setErrorMessage("Enter a valid country name");

        stateField.setPattern("^(?=.{2,100}$)[A-Za-z]+(?:\\s[A-Za-z]+)*$");
        stateField.setMaxLength(100);
        stateField.setErrorMessage("Enter a valid state name");

        cityField.setPattern("^(?=.{2,150}$)[A-Za-z]+(?:\\s[A-Za-z]+)*$");
        cityField.setMaxLength(150);
        cityField.setErrorMessage("Enter a valid city name");

        vendorNameField.setRequired(true);
        vendorEmailField.setRequired(true);

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
                postalCodeField);

        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        saveButton.addClickListener(e -> saveVendor());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        cancelButton.addClickListener(e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_ERROR);
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        add(formLayout, buttons);
    }

    private void saveVendor() {
        try {

            if (vendorNameField.isInvalid() || vendorEmailField.isInvalid() || vendorPhoneField.isInvalid()
                || postalCodeField.isInvalid()|| cityField.isInvalid() || stateField.isInvalid() || countryField.isInvalid()) {

            Notification.show( "Please correct validation errors",3000,Notification.Position.TOP_CENTER);
            return;
        }
            if (vendorNameField.isEmpty() || vendorEmailField.isEmpty()) {

                Notification.show("Please fill all required fields", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            Vendor vendor = new Vendor();
            vendor.setVendorName(vendorNameField.getValue().trim());
            vendor.setVendorEmail(vendorEmailField.getValue().trim());
            vendor.setVendorPhoneNumber(
                    vendorPhoneField.getValue().trim().equals("") ? null : vendorPhoneField.getValue());
            vendor.setActive(true);
            vendor.setCategories(new ArrayList<>(categoryField.getValue()));
            Address address = new Address();
            address.setAddressLine(addressLineField.getValue().trim());
            address.setStreet(streetField.getValue().trim());
            address.setCity(cityField.getValue().trim());
            address.setState(stateField.getValue().trim());
            address.setCountry(countryField.getValue().trim());
            address.setPostalCode(postalCodeField.getValue().trim());
            vendor.setVendorAddress(address);
            vendorService.addVendor(vendor, securityService.getLoggedInUser().getEmployee());

            Notification.show("Vendor Saved Successfully", 3000, Notification.Position.TOP_CENTER);
            close();
        } catch (Exception ex) {

            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER);
        }
    }
}