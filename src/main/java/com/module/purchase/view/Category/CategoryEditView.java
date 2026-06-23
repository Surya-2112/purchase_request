package com.module.purchase.view.category;

import java.time.LocalDate;
import java.util.Optional;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.enums.FrequencyType;
import com.module.purchase.enums.RepeatedPeriodReferType;
import com.module.purchase.service.CategoryService;
import com.module.purchase.service.RepeatedPeriodService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "category-edit", layout = MainLayout.class)
@PermitAll
public class CategoryEditView extends VerticalLayout implements HasUrlParameter<Long> {

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

        public CategoryEditView(CategoryService categoryService, RepeatedPeriodService repeatedPeriodService, 
                                SecurityService securityService) {
                this.categoryService = categoryService;
                this.repeatedPeriodService = repeatedPeriodService;
                this.securityService = securityService;

                setSizeFull();
                setPadding(true);

                categoryNameField.setRequired(true);
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

                FormLayout scheduleFormLayout = new FormLayout(frequencyPeriodField, frequencyTypeField, 
                        fromDateField, toDateField);
                scheduleFormLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

                scheduleSection.add(new H3("Automated RFQ Schedule Setup"), scheduleFormLayout);
                scheduleSection.setPadding(false);
                scheduleSection.setSpacing(true);
                scheduleSection.setVisible(false);

                autoRfqField.addValueChangeListener(event -> {
                        boolean isRfqEnabled = event.getValue();
                        scheduleSection.setVisible(isRfqEnabled);
                        if (isRfqEnabled) {
                                repeatableField.setValue(true);
                        }
                });
        }

        @Override
        public void setParameter(BeforeEvent event, Long categoryId) {

                removeAll();
                try{
                category = categoryService.getCategoryById(categoryId).orElse(null);

                if (category == null) {
                        add(new H2("Category Not Found"));
                        return;
                }

                H2 title = new H2("Update Category");

                categoryNameField.setValue(category.getCategoryName() == null ? "" : category.getCategoryName());
                repeatableField.setValue(category.isRepeatable());
                autoRfqField.setValue(category.isAutoRfq());

                if (category.isAutoRfq()) {
                        Optional<RepeatedPeriod> periodOpt = repeatedPeriodService
                                        .findByReferTypeAndReferId(RepeatedPeriodReferType.CATEGORY, category.getCategoryId());

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

                FormLayout baseFormLayout = new FormLayout();
                baseFormLayout.add(categoryNameField, repeatableField, autoRfqField);
                baseFormLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

                Button saveButton = new Button("Save");
                saveButton.addClickListener(e -> {
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

                                        if (fromDateField.getValue().isBefore(LocalDate.now()) && !category.isAutoRfq()) {
                                                Notification.show("Start Date cannot be a past date", 3000, Notification.Position.TOP_CENTER);
                                                return;
                                        }
                                }
                                category.setCategoryName(categoryNameField.getValue().trim());
                                category.setRepeatable(repeatableField.getValue());
                                category.setAutoRfq(autoRfqField.getValue());

                                categoryService.updateCategory(category, securityService.getLoggedInUser().getEmployee());

                                if (category.isAutoRfq()) {
                                        RepeatedPeriod period = repeatedPeriodService
                                                        .findByReferTypeAndReferId(RepeatedPeriodReferType.CATEGORY, category.getCategoryId())
                                                        .orElse(new RepeatedPeriod());

                                        period.setReferType(RepeatedPeriodReferType.CATEGORY);
                                        period.setReferId(category.getCategoryId());
                                        period.setFrequencyPeriod(frequencyPeriodField.getValue());
                                        period.setFrequencyType(frequencyTypeField.getValue());
                                        period.setFromDate(fromDateField.getValue());
                                        period.setToDate(toDateField.getValue());

                                        if (period.getId() == null || !fromDateField.getValue().equals(period.getFromDate())) {
                                                period.setNextDate(fromDateField.getValue());
                                        }

                                        repeatedPeriodService.save(period);
                                } else {
                                        repeatedPeriodService.deleteByReferTypeAndReferId(RepeatedPeriodReferType.CATEGORY, category.getCategoryId());
                                }

                                Notification.show("Category Updated Successfully", 3000, Notification.Position.TOP_CENTER);

                                getUI().ifPresent(ui -> ui.navigate("category-details/" + category.getCategoryId()));

                        } catch (Exception exception) {
                                Notification.show(exception.getMessage(), 5000, Notification.Position.TOP_CENTER);
                        }
                });

                Button cancelButton = new Button("Cancel");
                cancelButton.addClickListener(e -> getUI().ifPresent(ui -> 
                        ui.navigate("category-details/" + category.getCategoryId())));

                HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

                add(title, baseFormLayout, scheduleSection, buttons);
        }catch(Exception ex)
                {      event.forwardTo("category");
                       event.getUI().access(() -> {
                        Notification.show(ex.getMessage(),3000,Notification.Position.TOP_CENTER);
                        });
                                return;
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
}