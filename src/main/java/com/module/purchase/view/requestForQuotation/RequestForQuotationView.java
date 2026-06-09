package com.module.purchase.view.requestForQuotation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.enums.RequestForQuotationStatus;
import com.module.purchase.service.QuotationService;
import com.module.purchase.service.RequestForQuotationService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "request-for-quotation", layout = MainLayout.class)
@PermitAll
public class RequestForQuotationView extends VerticalLayout {

    private final RequestForQuotationService rfqService;
    private final SecurityService securityService;
    private final QuotationService quotationService;

    // Grids
    private final Grid<RequestForQuotation> rfqGrid = new Grid<>(RequestForQuotation.class, false);

    // Pagination Parameters
    private int currentPage = 0;
    private int pageSize = 25;
    private int totalPages = 1;
    private final Span pageInfo = new Span();

    // Filter Fields
    private final TextField rfqIdField = new TextField("RFQ ID");
    private final ComboBox<RequestForQuotationStatus> statusFilter = new ComboBox<>("Status");
    private final DatePicker requestedDateFilter = new DatePicker("Requested Date");

   public RequestForQuotationView(RequestForQuotationService rfqService, SecurityService securityService, QuotationService quotationService) {
        this.rfqService = rfqService;
        this.securityService = securityService;
        this.quotationService= quotationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // REMOVED statusFilter.setValue from here to prevent the IllegalStateException!

        buildUI();
        loadData();
    }

    private void buildUI() {
        H2 title = new H2("Requests For Quotations (RFQ)");

        Button addRfqButton = new Button("Add Request For Quotation");
        addRfqButton.addThemeName("primary success");
        addRfqButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("rfq-form")));
        addRfqButton.setVisible(securityService.canAccessView("rfq-form"));

        HorizontalLayout headerLayout = new HorizontalLayout(title, addRfqButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);

        // FIXED: Populate the list of items FIRST...
        statusFilter.setItems(RequestForQuotationStatus.values());
        // ...and NOW it is perfectly safe to set the default selection value!
        statusFilter.setValue(RequestForQuotationStatus.DRAFT); 
        
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("200px");
        
        rfqIdField.setPlaceholder("Search ID...");
        rfqIdField.setWidth("150px");

        Button search = new Button("Search", e -> {
            currentPage = 0;
            loadData();
        });
        search.addThemeName("primary");

        Button clear = new Button("Clear", e -> {
            rfqIdField.clear();
            statusFilter.setValue(RequestForQuotationStatus.DRAFT); 
            requestedDateFilter.clear();
            currentPage = 0;
            loadData();
        });

        HorizontalLayout filtersLayout = new HorizontalLayout(rfqIdField, statusFilter, requestedDateFilter, search, clear);
        filtersLayout.setAlignItems(Alignment.END);
        filtersLayout.setSpacing(true);
        
        // Grid Columns Definitions Layout
        rfqGrid.removeAllColumns();
        
        rfqGrid.addColumn(RequestForQuotation::getId).setHeader("RFQ ID").setWidth("110px").setFlexGrow(0);
        rfqGrid.addColumn(rfq -> rfq.getRequestedDate() != null ? rfq.getRequestedDate().toString() : "-").setHeader("Requested Date").setAutoWidth(true);
        rfqGrid.addColumn(rfq -> rfq.getRequestEndDate() != null ? rfq.getRequestEndDate().toString() : "-").setHeader("Closing/End Date").setAutoWidth(true);
        
        rfqGrid.addColumn(rfq -> quotationService.getCountByRFQ(rfq))
                .setHeader("Quotations Recv").setWidth("150px");

        gridStatusBadgeMapping(); // Dynamic styling colors assignment injection row helper

        rfqGrid.setWidthFull();
        rfqGrid.setHeightFull();
        rfqGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Double click routing to drill down details page layout panels
        rfqGrid.addItemDoubleClickListener(event -> {
            RequestForQuotation selectedRfq = event.getItem();
            getUI().ifPresent(ui -> ui.navigate("request-for-quotation-details/" + selectedRfq.getId()));
        });

        // Pagination layout configurations
        Button prev = new Button("Prev", event -> {
            if (currentPage > 0) {
                currentPage--;
                loadData();
            }
        });

        Button next = new Button("Next", event -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadData();
            }
        });

        ComboBox<Integer> pageSizeField = new ComboBox<>();
        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);
        pageSizeField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                pageSize = event.getValue();
                currentPage = 0;
                loadData();
            }
        });

        HorizontalLayout paginationLayout = new HorizontalLayout(prev, pageInfo, next, new Span("Page Size"), pageSizeField);
        paginationLayout.setWidthFull();
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setAlignItems(Alignment.CENTER);

        add(headerLayout, filtersLayout, rfqGrid, paginationLayout);
        expand(rfqGrid);
    }

   private void gridStatusBadgeMapping() {
        rfqGrid.addComponentColumn(rfq -> {
            Span badge = new Span(rfq.getStatus() != null ? rfq.getStatus().name() : "UNKNOWN");
            
            // Safe inline style pair declarations
            badge.getStyle()
                 .set("padding", "2px 8px")
                 .set("border-radius", "4px")
                 .set("font-weight", "bold")
                 .set("font-size", "12px");
            
            if (rfq.getStatus() == RequestForQuotationStatus.DRAFT) {
                badge.getStyle().set("background-color", "#f1f5f9").set("color", "#475569");
            } else if (rfq.getStatus() == RequestForQuotationStatus.OPEN) {
                badge.getStyle().set("background-color", "#e0f2fe").set("color", "#0369a1");
            } else if (rfq.getStatus() == RequestForQuotationStatus.CLOSED) {
                badge.getStyle().set("background-color", "#fee2e2").set("color", "#b91c1c");
            }
            
            return badge;
        }).setHeader("Status").setWidth("140px").setFlexGrow(0);
    }

    private void loadData() {
        Pageable pageable = PageRequest.of(currentPage, pageSize);
        
        RequestForQuotation filterCriteria = new RequestForQuotation();
        if (!rfqIdField.isEmpty()) {
            try {
                filterCriteria.setId(Long.valueOf(rfqIdField.getValue().trim()));
            } catch (NumberFormatException nfe) {
                // Ignore silent fallback if raw text strings entered accidentally
            }
        }
        filterCriteria.setStatus(statusFilter.getValue());
        filterCriteria.setRequestedDate(requestedDateFilter.getValue());

        // Calls your backend paged specification endpoint mapper cleanly
        Page<RequestForQuotation> page = rfqService.getRequestsForQuotationPaged(filterCriteria, pageable);

        if (page != null) {
            rfqGrid.setItems(page.getContent());
            this.totalPages = page.getTotalPages() > 0 ? page.getTotalPages() : 1;
            pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
        }
    }
}