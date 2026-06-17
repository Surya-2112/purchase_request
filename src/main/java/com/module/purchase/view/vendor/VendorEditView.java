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

    private final MultiSelectComboBox<Category> categoryField =new MultiSelectComboBox<>("Categories");

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

        activeField.setItems("Active", "Inactive");

        categoryField.setItems(categoryService.getCategories());
        categoryField.setItemLabelGenerator(Category::getCategoryName);
    }

    @Override
    public void setParameter(BeforeEvent event, Long vendorId) {

        removeAll();

        vendor = vendorService.getVendorById(vendorId).orElse(null);

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

        // FORM
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
                postalCodeField
        );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2)
        );

        // SAVE
        Button saveButton = new Button("Save", e -> {

            try {

                vendor.setVendorName(vendorNameField.getValue());
                vendor.setVendorPhoneNumber(vendorPhoneField.getValue().trim().equals("")?null:vendorPhoneField.getValue().trim());
                vendor.setActive("Active".equals(activeField.getValue()));

                vendor.setCategories(List.copyOf(categoryField.getValue()));

                Address updatedAddress = vendor.getVendorAddress();

                if (updatedAddress == null) {
                    updatedAddress = new Address();
                }

                updatedAddress.setAddressLine(addressLineField.getValue());
                updatedAddress.setStreet(streetField.getValue());
                updatedAddress.setCity(cityField.getValue());
                updatedAddress.setState(stateField.getValue());
                updatedAddress.setCountry(countryField.getValue());
                updatedAddress.setPostalCode(postalCodeField.getValue());

                vendor.setVendorAddress(updatedAddress);

                vendorService.updateVendor(
                        vendor,
                        securityService.getLoggedInUser().getEmployee()
                );

                Notification.show(
                        "Vendor Updated Successfully",
                        3000,
                        Notification.Position.TOP_CENTER
                );

                getUI().ifPresent(ui ->
                        ui.navigate("vendor-details/" + vendor.getVendorId())
                );

            } catch (Exception ex) {

                Notification.show(
                        ex.getMessage(),
                        5000,
                        Notification.Position.TOP_CENTER
                );
            }
        });

        Button cancelButton = new Button("Cancel", e ->
                getUI().ifPresent(ui ->
                        ui.navigate("vendor-details/" + vendor.getVendorId())
                )
        );

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        add(title, formLayout, buttons);
    }
}