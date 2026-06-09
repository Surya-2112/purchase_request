package com.module.purchase.view.quotation;

import java.util.List;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.Vendor;
import com.module.purchase.enums.Status;
import com.module.purchase.service.QuotationService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "quotations", layout = MainLayout.class)
@PermitAll
public class QuotationView extends VerticalLayout {

    private final QuotationService quotationService;
    private final SecurityService securityService;

    private final Grid<Quotation> quotationGrid = new Grid<>(Quotation.class, false);
    private final ComboBox<Status> statusFilter = new ComboBox<>("Filter by Status");
    
    private boolean isVendorUser = false;
    private Vendor loggedInVendor;

    public QuotationView(QuotationService quotationService, SecurityService securityService) {
        this.quotationService = quotationService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        evaluateUserRoleContext();
        buildUI();
        loadFilteredQuotationsData();
    }

    /**
     * CONTEXT EVALUATOR: Identifies if session belongs to internal employee or external supplier
     */
    private void evaluateUserRoleContext() {
        // Safe structural fallback validation parsing check
        if (securityService.getLoggedInUser().getVendor() != null) {
            this.isVendorUser = true;
            this.loggedInVendor = securityService.getLoggedInUser().getVendor();
        }
    }

    private void buildUI() {
        H2 title = new H2(isVendorUser ? "My Submitted Quotations" : "Supplier Quotations Ledger");
        Span subtitle = new Span(isVendorUser 
            ? "Track and manage your submitted pricing proposals and bid drafts."
            : "Review, evaluate, and process price quotes submitted by verified contractors.");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // 1. Setup Search Filters Toolbars Layout Panel
        statusFilter.setItems(Status.values());
        statusFilter.setClearButtonVisible(true);
        statusFilter.setPlaceholder("All Statuses");
        statusFilter.addValueChangeListener(e -> loadFilteredQuotationsData());

        Button refreshBtn = new Button("Refresh", VaadinIcon.REFRESH.create(), e -> loadFilteredQuotationsData());
        
        HorizontalLayout filterBar = new HorizontalLayout(statusFilter, refreshBtn);
        filterBar.setAlignItems(Alignment.END);

        // 2. Configure Columns Mapping Architecture Grid
        quotationGrid.addColumn(Quotation::getId).setHeader("Quote ID").setWidth("100px").setFlexGrow(0);
        quotationGrid.addColumn(q -> q.getRequestForQuotation() != null ? "RFQ-" + q.getRequestForQuotation().getId() : "-")
                .setHeader("Source RFQ ID").setAutoWidth(true);
        
        // Conditional Column Display: Employees need to see Supplier names, Vendors already know who they are!
        if (!isVendorUser) {
            quotationGrid.addColumn(q -> q.getVendor() != null ? q.getVendor().getVendorName() : "-")
                    .setHeader("Supplier Name").setAutoWidth(true);
        }

        quotationGrid.addColumn(q -> q.getQuotationDate() != null ? q.getQuotationDate().toString() : "-")
                .setHeader("Submission Date").setAutoWidth(true);
        
        quotationGrid.addColumn(q -> String.format("%.2f INR", q.getTotalAmount())).setHeader("Gross Total Cost Offer").setAutoWidth(true);

        // Inject dynamic color status badges mapping rules matrix
        mapStatusBadgeColumn();

        quotationGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        quotationGrid.setSizeFull();

        // 3. Drill-down details view routing loop interactions mappings
        quotationGrid.addItemDoubleClickListener(event -> {
            Quotation selectedQuote = event.getItem();
            // Navigates down to item sheet summaries layout (Shared or dedicated detail targets)
            getUI().ifPresent(ui -> ui.navigate("quotation-details/" + selectedQuote.getId()));
        });

        add(title, subtitle, filterBar, quotationGrid);
        expand(quotationGrid);
    }

    private void mapStatusBadgeColumn() {
        quotationGrid.addComponentColumn(q -> {
            Span badge = new Span(q.getStatus() != null ? q.getStatus().name() : "UNKNOWN");
            badge.getStyle()
                 .set("padding", "2px 8px")
                 .set("border-radius", "4px")
                 .set("font-weight", "bold")
                 .set("font-size", "12px");

            if (q.getStatus() == Status.DRAFT) {
                badge.getStyle().set("background-color", "#f1f5f9").set("color", "#475569");
            } else if (q.getStatus() == Status.APPROVED) {
                badge.getStyle().set("background-color", "#dcfce7").set("color", "#15803d"); // Green for active submissions bids
            } else if (q.getStatus() == Status.REJECTED) {
                badge.getStyle().set("background-color", "#fee2e2").set("color", "#b91c1c");
            } else {
                badge.getStyle().set("background-color", "#fef9c3").set("color", "#a16207"); // Pending/Under Review states
            }
            return badge;
        }).setHeader("Status").setWidth("130px").setFlexGrow(0);
    }

    /**
     * CORE LOADING LOGIC SYSTEM: Implements strict multi-tenant filtering profiles
     */
    private void loadFilteredQuotationsData() {
        List<Quotation> loadedDataset;

        if (isVendorUser) {
            // VENDOR RULES: Strict isolation. Can only fetch records tied directly to their profile id block
            List<Quotation> vendorAllQuotes = quotationService.getQuotationsByVendor(loggedInVendor);
            
            if (!statusFilter.isEmpty()) {
                loadedDataset = vendorAllQuotes.stream()
                        .filter(q -> q.getStatus() == statusFilter.getValue())
                        .toList();
            } else {
                loadedDataset = vendorAllQuotes;
            }
        } else {
            // EMPLOYEE RULES: Global access visibility across all records for procurement analysis
            if (!statusFilter.isEmpty()) {
                loadedDataset = quotationService.getAllQuotations().stream()
                        .filter(q -> q.getStatus() == statusFilter.getValue())
                        .toList();
            } else {
                loadedDataset = quotationService.getAllQuotations();
            }
        }

        quotationGrid.setItems(loadedDataset);
    }
}