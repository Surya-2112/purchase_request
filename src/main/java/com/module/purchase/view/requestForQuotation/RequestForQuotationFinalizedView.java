package com.module.purchase.view.requestForQuotation;

import java.util.ArrayList;
import java.util.List;

import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.RequestForQuotationLine;
import com.module.purchase.enums.Status;
import com.module.purchase.service.QuotationService; 
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

@Route(value = "rfq-finalized-view", layout = MainLayout.class)
@PermitAll
public class RequestForQuotationFinalizedView extends VerticalLayout implements HasUrlParameter<Long> {

    private final RequestForQuotationService rfqService;
    private final QuotationService quotationService; // Injected service field layer

    // Read-Only Structural Fields
    private final TextField rfqIdField = new TextField("RFQ Reference ID");
    private final DatePicker requestedDate = new DatePicker("Requested Date");
    private final DatePicker requestEndDate = new DatePicker("Quotation Closing / End Date");
    private final HorizontalLayout statusBadgeContainer = new HorizontalLayout();

    // Sourced Line Items Grid
    private final Grid<RequestForQuotationLine> detailsLinesGrid = new Grid<>(RequestForQuotationLine.class, false);
    private final List<RequestForQuotationLine> linesDataset = new ArrayList<>();

    // Historical Audit Evaluation Grids
    private final Grid<Quotation> approvedQuotationGrid = new Grid<>(Quotation.class, false);
    private final Grid<Quotation> rejectedQuotationsGrid = new Grid<>(Quotation.class, false);

    private final Button backBtn = new Button("Back to Evaluation Center");

    public RequestForQuotationFinalizedView(RequestForQuotationService rfqService, QuotationService quotationService) {
        this.rfqService = rfqService;
        this.quotationService = quotationService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        buildUI();
    }

    private void buildUI() {
        VerticalLayout scrollContent = new VerticalLayout();
        scrollContent.setWidthFull();
        scrollContent.setPadding(true);
        scrollContent.setSpacing(true);

        H2 pageTitle = new H2("Finalized Request for Quotation Archive");

        // Force complete read-only constraint mechanics across all form layers
        rfqIdField.setReadOnly(true);
        requestedDate.setReadOnly(true);
        requestEndDate.setReadOnly(true);

        // Plain text aesthetics optimization for dates
        requestedDate.addThemeName("small");
        requestedDate.getStyle().set("border", "none").set("background", "transparent").set("box-shadow", "none");
        requestEndDate.addThemeName("small");
        requestEndDate.getStyle().set("border", "none").set("background", "transparent").set("box-shadow", "none");

        FormLayout headerLayout = new FormLayout(rfqIdField, requestedDate, requestEndDate);
        headerLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));

        statusBadgeContainer.setAlignItems(Alignment.CENTER);
        HorizontalLayout statusSection = new HorizontalLayout(new Span("Finalized Lifecycle State: "), statusBadgeContainer);
        statusSection.setAlignItems(Alignment.CENTER);

        // Core demand table grid configuration
        detailsLinesGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null 
                ? line.getItemVariant().getItem().getItemName() : "").setHeader("Sourced Material Item").setAutoWidth(true);
        detailsLinesGrid.addColumn(line -> line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "")
                .setHeader("Specification Detail").setAutoWidth(true);
        detailsLinesGrid.addColumn(RequestForQuotationLine::getRequestedQuantity).setHeader("Quantity Demanded").setWidth("160px");

        detailsLinesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        detailsLinesGrid.setAllRowsVisible(true);
        detailsLinesGrid.setWidthFull();

        // 1. CONFIGURE HISTORICAL CONTRACT WINNER GRID
        setupQuotationGridStructureTemplate(approvedQuotationGrid);
        
        // 2. CONFIGURE COMPETING REJECTED BIDS GRID  
        setupQuotationGridStructureTemplate(rejectedQuotationsGrid);

        backBtn.setIcon(VaadinIcon.ARROW_LEFT.create());
        backBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("quotation-comparison")));

        scrollContent.add(
            pageTitle, 
            headerLayout, 
            statusSection, 
            new Hr(), 
            new H3("Linked Core Asset Demand Items Summary"), 
            detailsLinesGrid, 
            new Hr(),
            new H3("🏆 Awarded Winner Vendor Quotation Contract"),
            approvedQuotationGrid,
            new Hr(),
            new H3("❌ Competing Rejected Vendor Proposals"),
            rejectedQuotationsGrid,
            new Hr(), 
            backBtn
        );

        Scroller scroller = new Scroller(scrollContent);
        scroller.setSizeFull();
        add(scroller);
    }

    private void setupQuotationGridStructureTemplate(Grid<Quotation> grid) {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.setAllRowsVisible(true);
        grid.setWidthFull();

        grid.addColumn(q -> "QUOTE-" + q.getId()).setHeader("Quotation ID").setWidth("120px");
        grid.addColumn(q -> q.getVendor() != null ? q.getVendor().getVendorName() : "-").setHeader("Vendor Name").setAutoWidth(true);
        grid.addColumn(q -> q.getQuotationDate() != null ? q.getQuotationDate().toString() : "-").setHeader("Submission Date").setWidth("150px");
        grid.addColumn(q -> String.format("%.2f INR", q.getTotalAmount())).setHeader("Total Bid Offer").setWidth("160px");
        
        // Add itemized details lookup redirect button link layer
        grid.addComponentColumn(q -> {
            Button inspectBtn = new Button("View Cost Sheet", VaadinIcon.SEARCH.create());
            inspectBtn.addThemeName("secondary small");
            inspectBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("quotation-details/" + q.getId())));
            return inspectBtn;
        }).setHeader("Details").setWidth("150px");
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long id) {
        if (id == null) {
            getUI().ifPresent(ui -> ui.navigate("quotation-comparison"));
            return;
        }

        rfqService.getRequestForQuotationById(id).ifPresentOrElse(rfq -> {
            this.rfqIdField.setValue("RFQ-" + rfq.getId());
            this.requestedDate.setValue(rfq.getRequestedDate());
            this.requestEndDate.setValue(rfq.getRequestEndDate());

            renderStatusBadge(rfq.getStatus().name());

            // Load primary material line rows requirements entries 
            linesDataset.clear();
            linesDataset.addAll(rfqService.getLinesByRfqId(rfq.getId()));
            detailsLinesGrid.setItems(linesDataset);

            // Fetch and isolate associated supplier bids directly using core Status properties
            List<Quotation> allQuotes = quotationService.getQuotationsByRfq(rfq);

            List<Quotation> approvedWinner = allQuotes.stream()
                    .filter(q -> q.getStatus() == Status.APPROVED)
                    .toList();

            List<Quotation> rejectedBids = allQuotes.stream()
                    .filter(q -> q.getStatus() == Status.REJECTED)
                    .toList();

            approvedQuotationGrid.setItems(approvedWinner);
            rejectedQuotationsGrid.setItems(rejectedBids);

        }, () -> {
            Notification.show("Requested profile data missing from database log.", 4000, Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("quotation-comparison"));
        });
    }

    private void renderStatusBadge(String statusName) {
        statusBadgeContainer.removeAll();
        Span badge = new Span(statusName);
        badge.getStyle()
             .set("padding", "4px 12px").set("border-radius", "12px")
             .set("font-weight", "bold").set("font-size", "13px")
             .set("background-color", "#fee2e2").set("color", "#b91c1c"); 
        statusBadgeContainer.add(badge);
    }
}