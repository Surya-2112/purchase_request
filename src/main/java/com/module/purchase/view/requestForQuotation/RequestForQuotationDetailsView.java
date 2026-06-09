package com.module.purchase.view.requestForQuotation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entity.RequestForQuotationLine;
import com.module.purchase.entity.Vendor;
import com.module.purchase.enums.RequestForQuotationStatus;
import com.module.purchase.service.PurchaseRequestLineService;
import com.module.purchase.service.RequestForQuotationService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "request-for-quotation-details", layout = MainLayout.class)
@PermitAll
public class RequestForQuotationDetailsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final RequestForQuotationService rfqService;
    private final PurchaseRequestLineService prLineService;
    private final SecurityService securityService;

    private RequestForQuotation currentRfq;
    private boolean isVendorUser = false;
    private Vendor loggedInVendor;

    // Read-Only Profile Header Fields
    private final TextField rfqIdField = new TextField("RFQ Reference ID");
    private final TextField requestedDateField = new TextField("Requested Date");
    
    // Dynamic Date Control Panel Layout elements
    private final DatePicker requestEndDateField = new DatePicker("Quotation Closing / End Date");
    private final Button updateDateBtn = new Button("Extend / Adjust Closing Date");
    private final HorizontalLayout statusBadgeContainer = new HorizontalLayout();

    // Sourced Line Items Grid
    private final Grid<RequestForQuotationLine> detailsLinesGrid = new Grid<>(RequestForQuotationLine.class, false);
    private final List<RequestForQuotationLine> linesDataset = new ArrayList<>();

    // Action Row Buttons
    private final Button backBtn = new Button("Back to Dashboard");
    private final Button editBtn = new Button("Edit Draft Layout");
    private final Button cancelRfqBtn = new Button("Cancel RFQ"); 
    private final Button addQuotationBtn = new Button("Add Quotation Bid"); // ADDED: For Employee Administrative Overrides
    private final Button createQuotationBtn = new Button("Submit Quotation Bid"); // For Vendor Portal View

    public RequestForQuotationDetailsView(RequestForQuotationService rfqService, 
                                         PurchaseRequestLineService prLineService,
                                         SecurityService securityService) {
        this.rfqService = rfqService;
        this.prLineService = prLineService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        evaluateUserRoleContext();
        buildUI();
    }

    private void evaluateUserRoleContext() {
        if (securityService.getLoggedInUser() != null && securityService.getLoggedInUser().getVendor() != null) {
            this.isVendorUser = true;
            this.loggedInVendor = securityService.getLoggedInUser().getVendor();
        }
    }

    private void buildUI() {
        VerticalLayout scrollContent = new VerticalLayout();
        scrollContent.setWidthFull();
        scrollContent.setPadding(true);
        scrollContent.setSpacing(true);

        H2 pageTitle = new H2(isVendorUser ? "Review Invitation For Quotation" : "Request for Quotation Profile Summary");

        rfqIdField.setReadOnly(true);
        requestedDateField.setReadOnly(true);
        requestEndDateField.setReadOnly(true); 
        requestEndDateField.setMin(LocalDate.now().plusDays(1)); 

        FormLayout headerLayout = new FormLayout(rfqIdField, requestedDateField, requestEndDateField);
        headerLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));

        // Employee Date Adjustment Controls
        updateDateBtn.addThemeName("primary small");
        updateDateBtn.setIcon(VaadinIcon.CHECK.create());
        updateDateBtn.setVisible(false); 
        updateDateBtn.addClickListener(e -> saveClosingDateExtension());

        HorizontalLayout dateAdjustmentRow = new HorizontalLayout(updateDateBtn);
        dateAdjustmentRow.setPadding(false);

        statusBadgeContainer.setAlignItems(Alignment.CENTER);
        HorizontalLayout statusSection = new HorizontalLayout(new Span("Current Lifecycle State: "), statusBadgeContainer);
        statusSection.setAlignItems(Alignment.CENTER);

        // Setup Demanded Line Items Grid Columns
        detailsLinesGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null 
                ? line.getItemVariant().getItem().getItemName() : "").setHeader("Sourced Material Item").setAutoWidth(true);
        
        detailsLinesGrid.addColumn(line -> line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "")
                .setHeader("Specification Detail").setAutoWidth(true);
        
        detailsLinesGrid.addColumn(RequestForQuotationLine::getRequestedQuantity).setHeader("Quantity Demanded").setWidth("160px");

        detailsLinesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        detailsLinesGrid.setAllRowsVisible(true);
        detailsLinesGrid.setWidthFull();

        // Configure Navigation and Workspace Footers
        backBtn.setIcon(VaadinIcon.ARROW_LEFT.create());
        backBtn.addClickListener(e -> backToDashboard());

        // Employee: Modify Drafts action button parameters
        editBtn.addThemeName("primary warning");
        editBtn.setIcon(VaadinIcon.EDIT.create());
        editBtn.setVisible(false); 

        // Employee: Cancel Open RFQ action button parameters
        cancelRfqBtn.addThemeName("error primary");
        cancelRfqBtn.setIcon(VaadinIcon.CLOSE.create());
        cancelRfqBtn.setVisible(false);
        cancelRfqBtn.addClickListener(e -> executeCancelRfqRoutine());

        // Employee: Add Quotation Bid manual override action button parameters
        addQuotationBtn.addThemeName("primary success");
        addQuotationBtn.setIcon(VaadinIcon.PLUS.create());
        addQuotationBtn.setVisible(false);
        addQuotationBtn.addClickListener(e -> navigateToQuotationForm());

        // Vendor: Create Quotation Bid action button parameters
        createQuotationBtn.addThemeName("primary success");
        createQuotationBtn.setIcon(VaadinIcon.PENCIL.create());
        createQuotationBtn.setVisible(false);
        createQuotationBtn.addClickListener(e -> navigateToQuotationForm());

        HorizontalLayout actionsLayout = new HorizontalLayout(backBtn, editBtn, cancelRfqBtn, addQuotationBtn, createQuotationBtn);
        actionsLayout.setSpacing(true);

        scrollContent.add(pageTitle, headerLayout, dateAdjustmentRow, statusSection, new Hr(), 
                           new H3("Linked Core Asset Demand Items"), detailsLinesGrid, actionsLayout);

        Scroller scroller = new Scroller(scrollContent);
        scroller.setSizeFull();
        add(scroller);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long id) {
        if (id == null) {
            backToDashboard();
            return;
        }

        rfqService.getRequestForQuotationById(id).ifPresentOrElse(rfq -> {
            this.currentRfq = rfq;

            rfqIdField.setValue("RFQ-" + rfq.getId());
            requestedDateField.setValue(rfq.getRequestedDate() != null ? rfq.getRequestedDate().toString() : "-");
            requestEndDateField.setValue(rfq.getRequestEndDate());

            renderStatusBadge(rfq.getStatus());

            linesDataset.clear();
            linesDataset.addAll(rfqService.getLinesByRfqId(rfq.getId()));
            detailsLinesGrid.setItems(linesDataset);

            // DYNAMIC ROLE SEGREGATION MATRIX
            if (isVendorUser) {
                editBtn.setVisible(false);
                cancelRfqBtn.setVisible(false);
                addQuotationBtn.setVisible(false);
                updateDateBtn.setVisible(false);
                requestEndDateField.setReadOnly(true);
                
                createQuotationBtn.setVisible(rfq.getStatus() == RequestForQuotationStatus.OPEN);
            } else {
                createQuotationBtn.setVisible(false); 
                
                switch (rfq.getStatus()) {
                    case DRAFT -> {
                        editBtn.setVisible(true);
                        editBtn.addClickListener(click -> getUI().ifPresent(ui -> ui.navigate("rfq-form/" + rfq.getId())));
                        cancelRfqBtn.setVisible(false);
                        addQuotationBtn.setVisible(false);
                        requestEndDateField.setReadOnly(true);
                        updateDateBtn.setVisible(false);
                    }
                    case OPEN -> {
                        editBtn.setVisible(false);
                        cancelRfqBtn.setVisible(true); 
                        addQuotationBtn.setVisible(true); // Expose manual override selection layout tools to buyers
                        requestEndDateField.setReadOnly(false); 
                        updateDateBtn.setVisible(true);
                    }
                    default -> { 
                        editBtn.setVisible(false);
                        cancelRfqBtn.setVisible(false);
                        addQuotationBtn.setVisible(false);
                        requestEndDateField.setReadOnly(true);
                        updateDateBtn.setVisible(false);
                    }
                }
            }

        }, () -> {
            Notification.show("Requested profile data missing.", 4000, Position.MIDDLE);
            backToDashboard();
        });
    }

    private void navigateToQuotationForm() {
        if (this.currentRfq != null) {
            getUI().ifPresent(ui -> ui.navigate("quotation-form/new/" + currentRfq.getId()));
        }
    }

    private void saveClosingDateExtension() {
        if (requestEndDateField.isEmpty()) {
            Notification.show("Please enter a valid timeline closure date target.", 3000, Position.MIDDLE);
            return;
        }
        try {
            Employee executionActor = securityService.getLoggedInUser().getEmployee();
            currentRfq.setRequestEndDate(requestEndDateField.getValue());
            rfqService.updateRequestForQuotation(currentRfq, executionActor);

            Notification.show("RFQ Bidding closure extension saved successfully!", 3000, Position.TOP_CENTER);
            backToDashboard();
        } catch (Exception ex) {
            Notification.show("Database transaction rejected: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }

    private void executeCancelRfqRoutine() {
        if (this.currentRfq == null || this.currentRfq.getId() == null) return;

        try {
            Employee actor = securityService.getLoggedInUser().getEmployee();

            List<PurchaseRequestLine> connectedPrLines = prLineService.getRequestForQuotation(currentRfq);
            for (PurchaseRequestLine prLine : connectedPrLines) {
                prLine.setRequestForQuotation(null);
                prLineService.updatePurchaseRequestLine(prLine);
            }

            currentRfq.setStatus(RequestForQuotationStatus.CANCELLED);
            rfqService.updateRequestForQuotation(currentRfq, actor);

            Notification.show("Request for Quotation successfully cancelled. Demands unmapped safely.", 4000, Position.TOP_CENTER);
            backToDashboard();

        } catch (Exception ex) {
            Notification.show("Cancellation pipeline encountered an error: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }

    private void renderStatusBadge(RequestForQuotationStatus status) {
        statusBadgeContainer.removeAll();
        Span badge = new Span(status != null ? status.name() : "UNKNOWN");
        badge.getStyle()
             .set("padding", "4px 12px").set("border-radius", "12px")
             .set("font-weight", "bold").set("font-size", "13px");

        if (status == RequestForQuotationStatus.DRAFT) {
            badge.getStyle().set("background-color", "#f1f5f9").set("color", "#475569");
        } else if (status == RequestForQuotationStatus.OPEN) {
            badge.getStyle().set("background-color", "#e0f2fe").set("color", "#0369a1");
        } else if (status == RequestForQuotationStatus.CLOSED) {
            badge.getStyle().set("background-color", "#fee2e2").set("color", "#b91c1c");
        } else if (status == RequestForQuotationStatus.CANCELLED) {
            badge.getStyle().set("background-color", "#fef3c7").set("color", "#d97706"); 
        }
        statusBadgeContainer.add(badge);
    }

    private void backToDashboard() {
        if (isVendorUser) {
            getUI().ifPresent(ui -> ui.navigate("vendor-sourcing"));
        } else {
            getUI().ifPresent(ui -> ui.navigate("request-for-quotation"));
        }
    }
}