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

    private final Span scheduleId = new Span();
    private final Span intervalPattern = new Span();
    private final Span executionDates = new Span();
    private final Span referenceModule = new Span();

    private final VerticalLayout linkedItemContainer = new VerticalLayout();
    private final Span itemNameField = new Span();
    private final Span specField = new Span();
    private final Span quantityField = new Span();
    private final Span costField = new Span();

    private final Button backButton = new Button("Back to Schedules Grid");
    private final Button parentPrButton = new Button("Open Purchase Request");

    public RepeatedPeriodDetailsView(RepeatedPeriodService repeatedPeriodService,PurchaseRequestLineService lineService) {
        this.repeatedPeriodService = repeatedPeriodService;
        this.lineService = lineService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        buildUI();
    }

    private void buildUI() {
        H2 pageTitle = new H2("Scheduled Information");
        VerticalLayout profileCard = new VerticalLayout(
                new H3("Schedule Configuration"),
                scheduleId, referenceModule, intervalPattern, executionDates
        );
        profileCard.getStyle().set("background", "#f8fafc").set("border-radius", "8px").set("border", "1px solid #e2e8f0");
        profileCard.setSpacing(false);
        profileCard.setPadding(true);

        linkedItemContainer.add(
                new H3("Item for Purchase Request"),
                itemNameField, specField, quantityField, costField
        );
        linkedItemContainer.getStyle().set("background", "#f0fdf4").set("border-radius", "8px").set("border", "1px solid #bbf7d0");
        linkedItemContainer.setSpacing(false);
        linkedItemContainer.setPadding(true);
        linkedItemContainer.setVisible(false);

        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("repeated-periods")));
        
        parentPrButton.addThemeName("primary");
        parentPrButton.setVisible(false);

        HorizontalLayout actionControlsLayout = new HorizontalLayout(backButton, parentPrButton);
        actionControlsLayout.setSpacing(true);

        add(pageTitle, profileCard, new Hr(), linkedItemContainer, actionControlsLayout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long recordId = Long.parseLong(event.getRouteParameters().get("id").get());
        try{
        RepeatedPeriod record = repeatedPeriodService.getRepeatedPeriodById(recordId)
                .orElseThrow(() -> new RuntimeException("Sourcing rule profile was completely removed."));

        scheduleId.setText("Schedule Reference ID: " + record.getId());
        referenceModule.setText("Source: " + record.getReferType());
        intervalPattern.setText("Repeated Times " + record.getFrequencyPeriod() + " " + record.getFrequencyType());
        
        String start = record.getFromDate() != null ? record.getFromDate().toString() : "-";
        String end = record.getToDate() != null ? record.getToDate().toString() : "Indefinite Run Loop";
        String next = record.getNextDate() != null ? record.getNextDate().toString() : "Processing Queue...";
        
        executionDates.setText("From Date : " + start + ", To Date : " + end + ", Next Date : " + next);

        if (record.getReferType() == RepeatedPeriodReferType.PURCHASE_REQUEST_LINE && record.getReferId() != null) {
            Optional<PurchaseRequestLine> lineOpt = lineService.getPurchaseRequestLineById(record.getReferId());
            
            if (lineOpt.isPresent()) {
                PurchaseRequestLine targetLine = lineOpt.get();
                linkedItemContainer.setVisible(true);

                itemNameField.setText("Item Code : " + 
                        (targetLine.getItemVariant() != null && targetLine.getItemVariant().getItem() != null 
                        ? targetLine.getItemVariant().getItem().getItemName() : "Unknown Generic Asset"));
                
                specField.setText("Specification Detail : " + 
                        (targetLine.getItemVariant() != null ? targetLine.getItemVariant().getSpecification() : "-"));
                
                quantityField.setText("Requested Quantity : " + targetLine.getRequestedQuantity());
                
                double unitCost = targetLine.getItemUnitPrice() != null ? targetLine.getItemUnitPrice() : 0.0;
                costField.setText(" Unit Amount: " + unitCost);

                if (targetLine.getPurchaseRequestHeader() != null) {
                    Long headerId = targetLine.getPurchaseRequestHeader().getPurchaseRequestId();
                    parentPrButton.setVisible(true);
                    parentPrButton.addClickListener(clickEvent -> getUI().ifPresent(ui -> ui.navigate("purchase-request-details/" + headerId)));
                }
            } else {
                Notification.show("Notice: The original target line item row has been purged from active tracking tables.", 4000, Position.MIDDLE);
            }
        }
        }catch (Exception ex) {
            event.forwardTo("repeated-periods");
            event.getUI().access(() -> {
                Notification.show(ex.getMessage(), 4000, Position.MIDDLE);
            });
            return;
        } 
    }
}