package com.module.purchase.view.repeatedPeriod;

import java.util.Optional;

import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.enums.RepeatedPeriodReferType;
import com.module.purchase.service.PurchaseRequestLineService;
import com.module.purchase.service.RepeatedPeriodService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "repeated-period-details/:id", layout = MainLayout.class)
@PermitAll
public class RepeatedPeriodDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final RepeatedPeriodService repeatedPeriodService;
    private final PurchaseRequestLineService lineService;

    // Metric Fields Components
    private final Span scheduleId = new Span();
    private final Span intervalPattern = new Span();
    private final Span executionDates = new Span();
    private final Span referenceModule = new Span();

    // Associated Row Target Sub-items Details Components
    private final VerticalLayout linkedItemContainer = new VerticalLayout();
    private final Span itemNameField = new Span();
    private final Span specField = new Span();
    private final Span quantityField = new Span();
    private final Span costField = new Span();

    public RepeatedPeriodDetailsView(RepeatedPeriodService repeatedPeriodService,
                                     PurchaseRequestLineService lineService) {
        this.repeatedPeriodService = repeatedPeriodService;
        this.lineService = lineService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        buildUI();
    }

    private void buildUI() {
        H2 pageTitle = new H2("Recurring Schedule Profile Information");

        VerticalLayout profileCard = new VerticalLayout(
                new H3("Schedule Configuration Metrics"),
                scheduleId, referenceModule, intervalPattern, executionDates
        );
        profileCard.getStyle().set("background", "#f8fafc").set("border-radius", "8px").set("border", "1px solid #e2e8f0");
        profileCard.setSpacing(false);
        profileCard.setPadding(true);

        linkedItemContainer.add(
                new H3("Linked Core Asset / Item Metadata Snapshot"),
                itemNameField, specField, quantityField, costField
        );
        linkedItemContainer.getStyle().set("background", "#f0fdf4").set("border-radius", "8px").set("border", "1px solid #bbf7d0");
        linkedItemContainer.setSpacing(false);
        linkedItemContainer.setPadding(true);
        linkedItemContainer.setVisible(false); // Only displayed if mapping matches lines table row elements

        Button backButton = new Button("Back to Schedules Grid", e -> getUI().ifPresent(ui -> ui.navigate("repeated-periods")));
        
        Button parentPrButton = new Button("Open Parent Purchase Request", e -> {
            // Handled contextually based on entity associations setup step limits
        });
        parentPrButton.addThemeName("primary");
        parentPrButton.setVisible(false);

        HorizontalLayout actionControlsLayout = new HorizontalLayout(backButton, parentPrButton);
        actionControlsLayout.setSpacing(true);

        add(pageTitle, profileCard, new Hr(), linkedItemContainer, actionControlsLayout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long recordId = Long.parseLong(event.getRouteParameters().get("id").get());

        RepeatedPeriod record = repeatedPeriodService.getRepeatedPeriodById(recordId)
                .orElseThrow(() -> new RuntimeException("Sourcing rule profile was completely removed."));

        // Hydrate configuration visual string containers text parameters
        scheduleId.setText("Schedule Profile Reference ID: " + record.getId());
        referenceModule.setText("Originating Component Mapping Context: " + record.getReferType());
        intervalPattern.setText("Configured Recurrence Cycle: Every " + record.getFrequencyPeriod() + " " + record.getFrequencyType());
        executionDates.setText("Lifespan Horizon: From [" + record.getFromDate() + "] until [" + 
                (record.getToDate() != null ? record.getToDate().toString() : "Indefinite Run Loop") + 
                "] | Next Trigger Target: " + (record.getNextDate() != null ? record.getNextDate().toString() : "Processing Queue..."));

        // If it targets a PR Line item row, safely resolve relational fields across database tables
        if (record.getReferType() == RepeatedPeriodReferType.PURCHASE_REQUEST_LINE && record.getReferId() != null) {
            Optional<PurchaseRequestLine> lineOpt = lineService.getPurchaseRequestLineById(record.getReferId());
            
            if (lineOpt.isPresent()) {
                PurchaseRequestLine targetLine = lineOpt.get();
                linkedItemContainer.setVisible(true);

                itemNameField.setText("Item Classification Code: " + 
                        (targetLine.getItemVariant() != null && targetLine.getItemVariant().getItem() != null 
                        ? targetLine.getItemVariant().getItem().getItemName() : "Unknown Generic Asset"));
                
                specField.setText("Specification Detail Tag: " + 
                        (targetLine.getItemVariant() != null ? targetLine.getItemVariant().getSpecification() : "-"));
                
                quantityField.setText("Base Order Quantity Target per Loop Run: " + targetLine.getRequestedQuantity());
                
                double unitCost = targetLine.getItemUnitPrice() != null ? targetLine.getItemUnitPrice() : 0.0;
                costField.setText("Evaluated Estimated Unit Base Cost Amount: " + unitCost);

                // Dynamically append action click listener router linking straight back up to parent Document
                if (targetLine.getPurchaseRequestHeader() != null) {
                    Long headerId = targetLine.getPurchaseRequestHeader().getPurchaseRequestId();
                    
                    // Locate our view component instance out of array list stack collections layers structures
                    Button prNavigationBtn = (Button) ((HorizontalLayout) getComponentAt(4)).getComponentAt(1);
                    prNavigationBtn.setVisible(true);
                    prNavigationBtn.addClickListener(clickEvent -> getUI().ifPresent(ui -> ui.navigate("purchase-request-details/" + headerId)));
                }
            } else {
                Notification.show("Notice: The original target line item row has been purged from active tracking tables.", 4000, Position.MIDDLE);
            }
        }
    }
}