package com.module.purchase.view.vendor;

import java.util.List;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Address;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.Vendor;
import com.module.purchase.service.CategoryService;
import com.module.purchase.service.VendorService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "vendor-edit", layout = MainLayout.class)
@PermitAll
public class VendorEditView extends VerticalLayout implements HasUrlParameter<Long> {

    private final VendorService vendorService;
    private final CategoryService categoryService;
    private final SecurityService securityService;

    private final TextField vendorNameField = new TextField("Vendor Name");
    private final EmailField vendorEmailField = new EmailField("Vendor Email");
    private final TextField vendorPhoneField = new TextField("Vendor Phone");

    private final ComboBox<String> activeField = new ComboBox<>("Status");

    private final MultiSelectComboBox<Category> categoryField = new MultiSelectComboBox<>("Categories");

    private final TextField addressLineField = new TextField("Address Line");
    private final TextField streetField = new TextField("Street");
    private final TextField cityField = new TextField("City");
    private final TextField stateField = new TextField("State");
    private final TextField countryField = new TextField("Country");
    private final TextField postalCodeField = new TextField("Pincode");

    private Vendor vendor;

    public VendorEditView(
            VendorService vendorService,
            CategoryService categoryService,
            SecurityService securityService) {

        this.vendorService = vendorService;
        this.categoryService = categoryService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);

        vendorNameField.setRequired(true);
        vendorNameField.setPattern("^(?=.{3,72}$)[A-Za-z]+(?:[ '.][A-Za-z]+)*$");
        vendorNameField.setMaxLength(72);
        vendorNameField.setErrorMessage(
                "Enter a valid vendor name. Only letters, spaces, apostrophe and dot are allowed.");

        vendorEmailField.setMaxLength(100);

        vendorPhoneField.setPattern("^\\+?[0-9]{4,15}$");
        vendorPhoneField.setMaxLength(16);
        vendorPhoneField.setErrorMessage(
                "Enter a valid phone number with 4 to 15 digits");

        postalCodeField.setPattern("[0-9a-zA-Z]{3,10}");
        postalCodeField.setMaxLength(10);
        postalCodeField.setErrorMessage(
                "Enter a valid postal code (3-10 characters)");

        countryField.setPattern("^(?=.{2,50}$)[A-Za-z]+(?:\\s[A-Za-z]+)*$");
        countryField.setMaxLength(50);
        countryField.setErrorMessage("Enter a valid country name");

        stateField.setPattern("^(?=.{2,100}$)[A-Za-z]+(?:\\s[A-Za-z]+)*$");
        stateField.setMaxLength(100);
        stateField.setErrorMessage("Enter a valid state name");

        cityField.setPattern("^(?=.{2,150}$)[A-Za-z]+(?:\\s[A-Za-z]+)*$");
        cityField.setMaxLength(150);
        cityField.setErrorMessage("Enter a valid city name");

        activeField.setRequired(true);
        categoryField.setRequired(true);

        activeField.setItems("Active", "Inactive");

        categoryField.setItems(categoryService.getCategories());
        categoryField.setItemLabelGenerator(Category::getCategoryName);
    }

    @Override
    public void setParameter(BeforeEvent event, Long vendorId) {

        removeAll();

        if (securityService.getLoggedInUser().getVendor() != null) {
            if (!securityService.getLoggedInUser().getVendor().getVendorId().equals(vendorId)) {
                event.forwardTo("vendor");
                event.getUI().access(() -> {
                    Notification.show("Access Denied", 3000, Notification.Position.MIDDLE);
                });
                return;
            }
        }
        try {
            vendor = vendorService.getVendorById(vendorId).get();
        } catch (Exception ex) {
            event.forwardTo("vendor");
            event.getUI().access(() -> {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
            });
            return;
        }

        if (vendor == null) {
            add(new H2("Vendor Not Found"));
            return;
        }

        H2 title = new H2("Update Vendor");

        vendorNameField.setValue(vendor.getVendorName() == null ? "" : vendor.getVendorName());
        vendorEmailField.setValue(vendor.getVendorEmail() == null ? "" : vendor.getVendorEmail());
        vendorEmailField.setReadOnly(true);

        vendorPhoneField.setValue(vendor.getVendorPhoneNumber() == null ? "" : vendor.getVendorPhoneNumber());

        activeField.setValue(Boolean.TRUE.equals(vendor.getActive()) ? "Active" : "Inactive");

        if (vendor.getCategories() != null) {
            categoryField.setValue(vendor.getCategories());
        }

        Address address = vendor.getVendorAddress();
        if (address != null) {

            addressLineField.setValue(address.getAddressLine() == null ? "" : address.getAddressLine());
            streetField.setValue(address.getStreet() == null ? "" : address.getStreet());
            cityField.setValue(address.getCity() == null ? "" : address.getCity());
            stateField.setValue(address.getState() == null ? "" : address.getState());
            countryField.setValue(address.getCountry() == null ? "" : address.getCountry());
            postalCodeField.setValue(address.getPostalCode() == null ? "" : address.getPostalCode());
        }

        FormLayout formLayout = new FormLayout();

        formLayout.add(
                vendorNameField,
                vendorEmailField,
                vendorPhoneField,
                activeField,
                categoryField,
                addressLineField,
                streetField,
                cityField,
                stateField,
                countryField,
                postalCodeField);

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2));

        Button saveButton = new Button("Save", e -> {

            try {

                if (vendorNameField.isEmpty() || activeField.isEmpty()) {

                    Notification.show("Please fill all required fields", 3000, Notification.Position.MIDDLE);
                    return;
                }

                if (vendorNameField.isInvalid() || vendorPhoneField.isInvalid() || postalCodeField.isInvalid()
                    || cityField.isInvalid() || stateField.isInvalid() || countryField.isInvalid()) {

                    Notification.show("Please correct validation errors", 3000, Notification.Position.MIDDLE);
                    return;
                }

               vendor.setVendorName(vendorNameField.getValue().trim());

                vendor.setVendorPhoneNumber(vendorPhoneField.getValue().trim().isEmpty() ? null: vendorPhoneField.getValue().trim());
                vendor.setActive("Active".equals(activeField.getValue()));
                vendor.setCategories(List.copyOf(categoryField.getValue()));

                Address updatedAddress = vendor.getVendorAddress();

                if (updatedAddress == null) {
                    updatedAddress = new Address();
                }

               

                updatedAddress.setAddressLine(addressLineField.getValue().trim());

                updatedAddress.setStreet(streetField.getValue().trim());

                updatedAddress.setCity(cityField.getValue().trim());

                updatedAddress.setState(stateField.getValue().trim());

                updatedAddress.setCountry(countryField.getValue().trim());

                updatedAddress.setPostalCode(postalCodeField.getValue().trim());

                vendor.setVendorAddress(updatedAddress);

                vendorService.updateVendor(vendor, securityService.getLoggedInUser().getEmployee());

                Notification.show("Vendor Updated Successfully", 3000, Notification.Position.TOP_CENTER);

                getUI().ifPresent(ui -> ui.navigate("vendor-details/" + vendor.getVendorId()));

            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_CENTER);
            }
        });

        Button cancelButton = new Button("Cancel",
                e -> getUI().ifPresent(ui -> ui.navigate("vendor-details/" + vendor.getVendorId())));

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        add(title, formLayout, buttons);
    }
}