package com.module.purchase.view.purchaseRequest;

import java.time.LocalDate;
import java.util.function.Consumer;

import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.enums.FrequencyType;
import com.module.purchase.enums.RepeatedPeriodReferType;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.IntegerField;

public class AutoRfqScheduleDialog extends Dialog {

    private final IntegerField frequencyPeriod = new IntegerField("Repeat Every");
    private final ComboBox<FrequencyType> frequencyType = new ComboBox<>("Interval Type");
    private final DatePicker fromDate = new DatePicker("Start Date");
    private final DatePicker toDate = new DatePicker("End Date (Optional)");

    public AutoRfqScheduleDialog(Consumer<RepeatedPeriod> onSaveCallback) {
        setHeaderTitle("Configure Sourcing Loop Schedule");
        setWidth("450px");

        // Set up the interval multipliers
        frequencyPeriod.setMin(1);
        frequencyPeriod.setValue(1);
        frequencyPeriod.setRequiredIndicatorVisible(true);

        // Bind the standard interval units enum (DAYS, WEEKS, MONTHS, YEARS)
        frequencyType.setItems(FrequencyType.values());
        frequencyType.setItemLabelGenerator(FrequencyType::name);
        frequencyType.setRequired(true);

        // STALENESS GUARD: Enforce selection from today onwards
        fromDate.setMin(LocalDate.now());
        fromDate.setRequired(true);

        // Reactive logic: prevent the end date from being set before the start date
        fromDate.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                toDate.setMin(e.getValue());
            } else {
                toDate.setMin(LocalDate.now());
            }
        });

        // Form Presentation Grid Layout
        FormLayout formLayout = new FormLayout(frequencyPeriod, frequencyType, fromDate, toDate);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        Button confirmBtn = new Button("Apply Schedule");
        confirmBtn.addThemeName("primary");
        confirmBtn.addClickListener(e -> {
            if (frequencyPeriod.isEmpty() || frequencyType.isEmpty() || fromDate.isEmpty()) {
                Notification.show("Please populate all required timeline configuration inputs.", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            // Construct and package data payload
            RepeatedPeriod period = new RepeatedPeriod();
            period.setReferType(RepeatedPeriodReferType.PURCHASE_REQUEST_LINE);
            period.setFrequencyPeriod(frequencyPeriod.getValue());
            period.setFrequencyType(frequencyType.getValue());
            period.setFromDate(fromDate.getValue());
            period.setToDate(toDate.getValue());
            
            // Default first execution milestone pointer to match the start boundary
            period.setNextDate(fromDate.getValue());

            // Fire functional callback up to the parent PurchaseRequestFormView line-handler
            onSaveCallback.accept(period);
            close();
        });

        Button cancelBtn = new Button("Cancel", e -> close());

        getFooter().add(confirmBtn, cancelBtn);
        add(formLayout);
    }
}
