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

    // Action Panel Components
    private final Button backBtn = new Button("Back to Ledger");
    private final Button editBtn = new Button("Edit Draft Workspace"); // ADDED
    private final Button submitFinalBtn = new Button("Submit & Finalize Bid"); // ADDED
    private final Button deleteBtn = new Button("Delete Quotation Draft"); // ADDED

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

        quoteIdField.setReadOnly(true);
        rfqIdField.setReadOnly(true);
        vendorNameField.setReadOnly(true);
        dateField.setReadOnly(true);
        grossTotalField.setReadOnly(true);

        FormLayout summaryLayout = new FormLayout();
        summaryLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));
        
        if (isVendorUser) {
            summaryLayout.add(quoteIdField, rfqIdField, dateField, grossTotalField);
        } else {
            summaryLayout.add(quoteIdField, rfqIdField, vendorNameField, dateField, grossTotalField);
        }

        statusBadgeContainer.setAlignItems(Alignment.CENTER);
        HorizontalLayout statusSection = new HorizontalLayout(new Span("Proposal Evaluation State: "), statusBadgeContainer);
        statusSection.setAlignItems(Alignment.CENTER);

        // Configure Proposal Lines Grid Columns
        linesGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null 
                ? line.getItemVariant().getItem().getItemName() : "").setHeader("Item Material Sourced").setAutoWidth(true);
        
        linesGrid.addColumn(line -> line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "")
                .setHeader("Specification Detail").setAutoWidth(true);
        
        linesGrid.addColumn(line -> String.format("%.2f INR", line.getUnitPrice())).setHeader("Offered Unit Price").setWidth("160px");

        linesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        linesGrid.setAllRowsVisible(true);
        linesGrid.setWidthFull();

        linesGrid.addSelectionListener(selection -> {
            if (selection.getFirstSelectedItem().isPresent()) {
                loadDiscountSlabBreakdownMatrix(selection.getFirstSelectedItem().get());
            } else {
                discountMatrixGrid.setItems(new ArrayList<>());
            }
        });

        // Configure Nested Volume Discount Matrices Grid Columns
        discountMatrixGrid.addColumn(DiscountType::getFromQuantity).setHeader("From Quantity (Slab Start)").setAutoWidth(true);
        discountMatrixGrid.addColumn(DiscountType::getToQuantity).setHeader("To Quantity (Slab End)").setAutoWidth(true);
        discountMatrixGrid.addColumn(d -> String.format("%.1f %%", d.getDiscountPercentage())).setHeader("Discount Percentage Deducted").setAutoWidth(true);
        
        discountMatrixGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        discountMatrixGrid.setAllRowsVisible(true);
        discountMatrixGrid.setWidthFull();

        // Standard Navigation setup
        backBtn.setIcon(VaadinIcon.ARROW_LEFT.create());
        backBtn.addClickListener(e -> backToDashboard());

        // Configuration Layer for dynamic Draft Buttons
        editBtn.addThemeName("primary warning");
        editBtn.setIcon(VaadinIcon.EDIT.create());
        editBtn.setVisible(false);
        editBtn.addClickListener(e -> {
            if (currentQuotation != null && currentQuotation.getRequestForQuotation() != null) {
                // Reroutes cleanly back to the workspace pricing input sheet canvas
                getUI().ifPresent(ui -> ui.navigate("quotation-form/new/" + currentQuotation.getRequestForQuotation().getId()));
            }
        });

        submitFinalBtn.addThemeName("primary success");
        submitFinalBtn.setIcon(VaadinIcon.PAPERPLANE.create());
        submitFinalBtn.setVisible(false);
        submitFinalBtn.addClickListener(e -> executeFinalizeDraftWorkflow());

        deleteBtn.addThemeName("error primary");
        deleteBtn.setIcon(VaadinIcon.TRASH.create());
        deleteBtn.setVisible(false);
        deleteBtn.addClickListener(e -> executeDraftDeletionRoutine());

        HorizontalLayout actionsLayout = new HorizontalLayout(backBtn, editBtn, submitFinalBtn, deleteBtn);
        actionsLayout.setSpacing(true);

        scrollContent.add(pageTitle, summaryLayout, statusSection, new Hr(), 
                           new H3("Offered Item Sourcing Base Estimates Price Breakdowns"), 
                           new Span("Click any row below to populate its corresponding Volume Tier Slab Discounts structure matrix."),
                           linesGrid, new Hr(),
                           new H3("Volume / Quantity Tier Slab Discount Matrix"), discountMatrixGrid, 
                           new Hr(), actionsLayout);

        Scroller viewScroller = new Scroller(scrollContent);
        viewScroller.setSizeFull();
        add(viewScroller);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long id) {
        if (id == null) {
            backToDashboard();
            return;
        }

        quotationService.getQuotationById(id).ifPresentOrElse(quote -> {
            this.currentQuotation = quote;

            quoteIdField.setValue("QUOTE-" + quote.getId());
            rfqIdField.setValue(quote.getRequestForQuotation() != null ? "RFQ-" + quote.getRequestForQuotation().getId() : "-");
            vendorNameField.setValue(quote.getVendor() != null ? quote.getVendor().getVendorName() : "-");
            dateField.setValue(quote.getQuotationDate() != null ? quote.getQuotationDate().toString() : "-");
            grossTotalField.setValue(String.format("%.2f INR", quote.getTotalAmount()));

            renderStatusBadge(quote.getStatus());

            linesDataset.clear();
            linesDataset.addAll(quotationService.getLinesByQuotation(quote));
            linesGrid.setItems(linesDataset);
            
            discountMatrixGrid.setItems(new ArrayList<>());

            // DYNAMIC BUTTON TOGGLE LOGIC BASED ON STATUS VALUE MATCHES
            if (quote.getStatus() == Status.DRAFT) {
                editBtn.setVisible(true);
                submitFinalBtn.setVisible(true);
                deleteBtn.setVisible(true);
            } else {
                editBtn.setVisible(false);
                submitFinalBtn.setVisible(false);
                deleteBtn.setVisible(false);
            }

        }, () -> {
            Notification.show("The selected proposal mapping is missing from active storage logs.", 4000, Position.MIDDLE);
            backToDashboard();
        });
    }

    private void loadDiscountSlabBreakdownMatrix(QuotationLine line) {
        List<DiscountType> activeDiscounts = quotationService.getDiscountsByLine(line);
        discountMatrixGrid.setItems(activeDiscounts);
    }

 
    private void executeFinalizeDraftWorkflow() {
        if (currentQuotation == null) return;
        try {
            currentQuotation.setStatus(Status.WAITING_APPROVAL);
            quotationService.updateQuotation(currentQuotation);
            Notification.show("Quotation final pricing proposal successfully finalized and opened for bidding evaluation!", 4000, Position.TOP_CENTER);
            backToDashboard();
        } catch (Exception ex) {
            Notification.show("Submission commit rejected: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }
    private void executeDraftDeletionRoutine() {
        if (currentQuotation == null || currentQuotation.getId() == null) return;
        try {
            quotationService.deleteQuotation(currentQuotation.getId());
            Notification.show("Draft proposal dropped and storage space unmapped safely.", 4000, Position.TOP_CENTER);
            backToDashboard();
        } catch (Exception ex) {
            Notification.show("Deletion protocol faulted: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
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
        } else if (status == Status.CANCELLED) {
            badge.getStyle().set("background-color", "#fef3c7").set("color", "#d97706");
        } else {
            badge.getStyle().set("background-color", "#fef9c3").set("color", "#a16207");
        }
        statusBadgeContainer.add(badge);
    }

    private void backToDashboard() {
        if (isVendorUser) {
            getUI().ifPresent(ui -> ui.navigate("vendor-sourcing"));
        } else {
            getUI().ifPresent(ui -> ui.navigate("quotations"));
        }
    }
}