package com.module.purchase.view.quotation;

import java.util.ArrayList;
import java.util.List;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.DiscountType;
import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.QuotationLine;
import com.module.purchase.enums.Status;
import com.module.purchase.service.QuotationService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
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

@Route(value = "quotation-details", layout = MainLayout.class)
@PermitAll
public class QuotationDetailsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final QuotationService quotationService;
    private final SecurityService securityService;

    private Quotation currentQuotation;
    private boolean isVendorUser = false;

    // Read-Only Profile Header Summary Fields
    private final TextField quoteIdField = new TextField("Quotation ID Reference");
    private final TextField rfqIdField = new TextField("Source RFQ Reference");
    private final TextField vendorNameField = new TextField("Vendor / Contractor Name");
    private final TextField dateField = new TextField("Date of Submission");
    private final TextField grossTotalField = new TextField("Total Cost Offer");
    private final HorizontalLayout statusBadgeContainer = new HorizontalLayout();

    // Master Quotation Lines Matrix Grid
    private final Grid<QuotationLine> linesGrid = new Grid<>(QuotationLine.class, false);
    private final List<QuotationLine> linesDataset = new ArrayList<>();

    // Child Nested Slab Discounts Grid
    private final Grid<DiscountType> discountMatrixGrid = new Grid<>(DiscountType.class, false);

    // Navigation Footers
    private final Button backBtn = new Button("Back to Ledger");

    public QuotationDetailsView(QuotationService quotationService, SecurityService securityService) {
        this.quotationService = quotationService;
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
        }
    }

    private void buildUI() {
        VerticalLayout scrollContent = new VerticalLayout();
        scrollContent.setWidthFull();
        scrollContent.setPadding(true);
        scrollContent.setSpacing(true);

        H2 pageTitle = new H2("Quotation Financial Proposal Details");

        // 1. Force Read-Only properties on Header Forms
        quoteIdField.setReadOnly(true);
        rfqIdField.setReadOnly(true);
        vendorNameField.setReadOnly(true);
        dateField.setReadOnly(true);
        grossTotalField.setReadOnly(true);

        FormLayout summaryLayout = new FormLayout();
        summaryLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));
        
        // Contextual Layout packing: Vendors don't need to read their own company name field row
        if (isVendorUser) {
            summaryLayout.add(quoteIdField, rfqIdField, dateField, grossTotalField);
        } else {
            summaryLayout.add(quoteIdField, rfqIdField, vendorNameField, dateField, grossTotalField);
        }

        statusBadgeContainer.setAlignItems(Alignment.CENTER);
        HorizontalLayout statusSection = new HorizontalLayout(new Span("Proposal Evaluation State: "), statusBadgeContainer);
        statusSection.setAlignItems(Alignment.CENTER);

        // 2. Configure Proposal Lines Grid Columns
        linesGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null 
                ? line.getItemVariant().getItem().getItemName() : "").setHeader("Item Material Sourced").setAutoWidth(true);
        
        linesGrid.addColumn(line -> line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "")
                .setHeader("Specification Detail").setAutoWidth(true);
        
        linesGrid.addColumn(line -> String.format("%.2f INR", line.getUnitPrice())).setHeader("Offered Unit Price").setWidth("160px");

        linesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        linesGrid.setAllRowsVisible(true);
        linesGrid.setWidthFull();

        // INTERACTIVE SUB-GRID LINK: Selecting a line row instantly exposes its bulk volume discount structure matrix!
        linesGrid.addSelectionListener(selection -> {
            if (selection.getFirstSelectedItem().isPresent()) {
                loadDiscountSlabBreakdownMatrix(selection.getFirstSelectedItem().get());
            } else {
                discountMatrixGrid.setItems(new ArrayList<>());
            }
        });

        // 3. Configure Nested Volume Discount Matrices Grid Columns
        discountMatrixGrid.addColumn(DiscountType::getFromQuantity).setHeader("From Quantity (Slab Start)").setAutoWidth(true);
        discountMatrixGrid.addColumn(DiscountType::getToQuantity).setHeader("To Quantity (Slab End)").setAutoWidth(true);
        discountMatrixGrid.addColumn(d -> String.format("%.1f %%", d.getDiscountPercentage())).setHeader("Discount Percentage Deducted").setAutoWidth(true);
        
        discountMatrixGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        discountMatrixGrid.setAllRowsVisible(true);
        discountMatrixGrid.setWidthFull();

        // 4. Action buttons setup
        backBtn.setIcon(VaadinIcon.ARROW_LEFT.create());
        backBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("quotations")));

        scrollContent.add(pageTitle, summaryLayout, statusSection, new Hr(), 
                           new H3("Offered Item Sourcing Base Estimates Price Breakdowns"), 
                           new Span("ℹ️ Click any row below to populate its corresponding Volume Tier Slab Discounts structure matrix."),
                           linesGrid, new Hr(),
                           new H3("Volume / Quantity Tier Slab Discount Matrix"), discountMatrixGrid, 
                           new Hr(), backBtn);

        Scroller viewScroller = new Scroller(scrollContent);
        viewScroller.setSizeFull();
        add(viewScroller);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long id) {
        if (id == null) {
            getUI().ifPresent(ui -> ui.navigate("quotations"));
            return;
        }

        quotationService.getQuotationById(id).ifPresentOrElse(quote -> {
            this.currentQuotation = quote;

            // Map layout properties values
            quoteIdField.setValue("QUOTE-" + quote.getId());
            rfqIdField.setValue(quote.getRequestForQuotation() != null ? "RFQ-" + quote.getRequestForQuotation().getId() : "-");
            vendorNameField.setValue(quote.getVendor() != null ? quote.getVendor().getVendorName() : "-");
            dateField.setValue(quote.getQuotationDate() != null ? quote.getQuotationDate().toString() : "-");
            grossTotalField.setValue(String.format("%.2f INR", quote.getTotalAmount()));

            renderStatusBadge(quote.getStatus());

            // Pull items datasets arrays rows sequentially down tables
            linesDataset.clear();
            linesDataset.addAll(quotationService.getLinesByQuotation(quote));
            linesGrid.setItems(linesDataset);
            
            // Clean active display arrays indices references profiles states pointers bounds
            discountMatrixGrid.setItems(new ArrayList<>());

        }, () -> {
            Notification.show("The selected proposal mapping is missing from active storage logs.", 4000, Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("quotations"));
        });
    }

    private void loadDiscountSlabBreakdownMatrix(QuotationLine line) {
        List<DiscountType> activeDiscounts = quotationService.getDiscountsByLine(line);
        discountMatrixGrid.setItems(activeDiscounts);
    }

    private void renderStatusBadge(Status status) {
        statusBadgeContainer.removeAll();
        Span badge = new Span(status != null ? status.name() : "UNKNOWN");
        badge.getStyle()
             .set("padding", "4px 12px").set("border-radius", "12px")
             .set("font-weight", "bold").set("font-size", "13px");

        if (status == Status.DRAFT) {
            badge.getStyle().set("background-color", "#f1f5f9").set("color", "#475569");
        } else if (status == Status.APPROVED) {
            badge.getStyle().set("background-color", "#dcfce7").set("color", "#15803d");
        } else if (status == Status.REJECTED) {
            badge.getStyle().set("background-color", "#fee2e2").set("color", "#b91c1c");
        } else {
            badge.getStyle().set("background-color", "#fef9c3").set("color", "#a16207");
        }
        statusBadgeContainer.add(badge);
    }
}