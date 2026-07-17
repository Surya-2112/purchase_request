package com.module.purchase.view.category;

import java.time.LocalDate;
import java.util.Optional;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.enums.FrequencyType;
import com.module.purchase.enums.RequestForQuotationStatus;
import com.module.purchase.enums.RepeatedPeriodReferType;
import com.module.purchase.service.CategoryService;
import com.module.purchase.service.RepeatedPeriodService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

public class CategoryForm extends Dialog {

    private final CategoryService categoryService;
    private final RepeatedPeriodService repeatedPeriodService;
    private final SecurityService securityService;

    private final TextField categoryNameField = new TextField("Category Name");
    private final Checkbox repeatableField = new Checkbox("Is Repeatable Category?");
    private final Checkbox autoRfqField = new Checkbox("Send RFQ Automatically?");

    private final VerticalLayout scheduleSection = new VerticalLayout();
    private final IntegerField frequencyPeriodField = new IntegerField("Repeat Every");
    private final ComboBox<FrequencyType> frequencyTypeField = new ComboBox<>("Interval Type");
    private final DatePicker fromDateField = new DatePicker("Start Date");
    private final DatePicker toDateField = new DatePicker("End Date (Optional)");

    private Category category;

    public CategoryForm(CategoryService categoryService, RepeatedPeriodService repeatedPeriodService, SecurityService securityService) {
        
        this.categoryService = categoryService;
        this.repeatedPeriodService = repeatedPeriodService;
        this.securityService = securityService; 

        setHeaderTitle("Add Category");
        setWidth("550px");

        categoryNameField.setRequiredIndicatorVisible(true);

        frequencyPeriodField.setMin(1);
        frequencyPeriodField.setValue(1);
        frequencyTypeField.setItems(FrequencyType.values());
        frequencyTypeField.setItemLabelGenerator(FrequencyType::name);

        fromDateField.setMin(LocalDate.now());
        fromDateField.setRequired(true);

        fromDateField.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                toDateField.setMin(e.getValue());
            } else {
                toDateField.setMin(LocalDate.now());
            }
        });

        FormLayout scheduleFormLayout = new FormLayout( frequencyPeriodField, frequencyTypeField,  fromDateField, toDateField );
        scheduleFormLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        
        scheduleSection.add(new H3("Automated RFQ Schedule Setup"), scheduleFormLayout);
        scheduleSection.setPadding(false);
        scheduleSection.setSpacing(true);
        scheduleSection.setVisible(false);

        autoRfqField.addValueChangeListener(event -> {
            boolean isRfqEnabled = event.getValue();
            scheduleSection.setVisible(isRfqEnabled);
        });

        FormLayout baseCategoryFormLayout = new FormLayout(categoryNameField, repeatableField, autoRfqField);
        baseCategoryFormLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        saveButton.addClickListener(e -> saveCategory());

        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_ERROR);
        cancelButton.addClickListener(e -> close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        VerticalLayout mainContainer = new VerticalLayout(baseCategoryFormLayout, scheduleSection, buttons);
        mainContainer.setPadding(false);
        mainContainer.setSpacing(true);
        
        add(mainContainer);
    }

    public void setCategory(Category category) {
        this.category = category;

        fromDateField.setMin(LocalDate.now());

        if (category != null) {
            categoryNameField.setValue(category.getCategoryName() == null ? "" : category.getCategoryName());
            repeatableField.setValue(category.isRepeatable());
            autoRfqField.setValue(category.isAutoRfq());

            setHeaderTitle("Update Category");

            if (category.isAutoRfq()) {
                Optional<RepeatedPeriod> periodOpt = repeatedPeriodService.findByReferTypeAndReferId(RepeatedPeriodReferType.CATEGORY, category.getCategoryId());
                
                if (periodOpt.isPresent()) {
                    RepeatedPeriod period = periodOpt.get();
                    frequencyPeriodField.setValue(period.getFrequencyPeriod());
                    frequencyTypeField.setValue(period.getFrequencyType());
                    
                    if (period.getFromDate() != null && period.getFromDate().isBefore(LocalDate.now())) {
                        fromDateField.setMin(period.getFromDate());
                    }
                    
                    fromDateField.setValue(period.getFromDate());
                    toDateField.setValue(period.getToDate());
                }
                scheduleSection.setVisible(true);
            } else {
                clearScheduleFields();
                scheduleSection.setVisible(false);
            }
        } else {
            categoryNameField.clear();
            repeatableField.clear();
            autoRfqField.clear();
            clearScheduleFields();
            scheduleSection.setVisible(false);
            setHeaderTitle("Add Category");
        }
    }

    private void clearScheduleFields() {
        frequencyPeriodField.setValue(1);
        frequencyTypeField.clear();
        fromDateField.clear();
        toDateField.clear();
        fromDateField.setMin(LocalDate.now());
        toDateField.setMin(LocalDate.now());
    }

    private void saveCategory() {
        try {
            if (categoryNameField.isEmpty()) {
                Notification.show("Category Name is required", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            if (autoRfqField.getValue()) {
                if (frequencyTypeField.isEmpty() || fromDateField.isEmpty()) {
                    Notification.show("Please populate all required RFQ repetition schedule parameters", 3000, Notification.Position.TOP_CENTER);
                    return;
                }
                
                if (fromDateField.getValue().isBefore(LocalDate.now()) && (category == null || !category.isAutoRfq())) {
                    Notification.show("Start Date cannot be a past date", 3000, Notification.Position.TOP_CENTER);
                    return;
                }
            }

            if (category == null) {
                category = new Category();
            }

            category.setCategoryName(categoryNameField.getValue().trim());
            category.setRepeatable(repeatableField.getValue());
            category.setAutoRfq(autoRfqField.getValue());

            Category savedCategory = categoryService.addCategory(category, securityService.getLoggedInUser().getEmployee());

            if (savedCategory.isAutoRfq()) {
                RepeatedPeriod period = repeatedPeriodService
                        .findByReferTypeAndReferId(RepeatedPeriodReferType.CATEGORY, savedCategory.getCategoryId())
                        .orElse(new RepeatedPeriod());

                period.setReferType(RepeatedPeriodReferType.CATEGORY);
                period.setReferId(savedCategory.getCategoryId());
                period.setFrequencyPeriod(frequencyPeriodField.getValue());
                period.setFrequencyType(frequencyTypeField.getValue());
                period.setFromDate(fromDateField.getValue());
                period.setToDate(toDateField.getValue());
                period.setStatus(RequestForQuotationStatus.OPEN);
                
                if (period.getId() == null || !fromDateField.getValue().equals(period.getFromDate())) {
                    period.setNextDate(period.getFrequencyType().calculateNext(period.getFromDate(),period.getFrequencyPeriod()));
                }
                repeatedPeriodService.addRepeatedPeriod(period,securityService.getLoggedInUser().getEmployee());
            } else {
                repeatedPeriodService.deleteByReferTypeAndReferId(RepeatedPeriodReferType.CATEGORY, savedCategory.getCategoryId());
            }

            Notification.show("Category and Schedules Saved Successfully", 3000, Notification.Position.TOP_CENTER);
            close();

        } catch (Exception exception) {
            Notification.show("Error: " + exception.getMessage(), 5000, Notification.Position.TOP_CENTER);
        }
    }
}