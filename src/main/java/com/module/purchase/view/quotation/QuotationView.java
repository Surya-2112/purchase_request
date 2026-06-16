package com.module.purchase.view.quotation;

import java.util.Arrays;
import java.util.List;
import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Vendor;
import com.module.purchase.entity.Users;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entityDTO.QuotationDTO;
import com.module.purchase.enums.Status;
import com.module.purchase.service.QuotationService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "quotations", layout = MainLayout.class)
@PermitAll
public class QuotationView extends VerticalLayout {

    private final QuotationService quotationService;
    private final SecurityService securityService;

    private final Grid<QuotationDTO> quotationGrid = new Grid<>(QuotationDTO.class, false);

    private int currentPage = 0;
    private int pageSize = 25;
    private int totalPages = 1;
    private final Span pageInfo = new Span();

    private boolean isVendorUser = false;
    private Vendor loggedInVendor;

    private QuotationDTO qFilter = new QuotationDTO();

    private final TextField quoteIdField = new TextField("Quote ID");
    private final TextField rfqIdField = new TextField("Source RFQ ID");
    private final TextField supplierField = new TextField("Supplier Name");
    private final ComboBox<Status> statusField = new ComboBox<>("Status");

    private HorizontalLayout filterBar;
    private final HorizontalLayout paginationLayout = new HorizontalLayout();

    public QuotationView(QuotationService quotationService, SecurityService securityService) {
        this.quotationService = quotationService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        evaluateUserRoleContext();
        buildUI();
        configureDefaultStatusFilters();
        loadData();
    }

    private void evaluateUserRoleContext() {
        Users user = securityService.getLoggedInUser();
        if (user != null && user.getVendor() != null) {
            this.isVendorUser = true;
            this.loggedInVendor = user.getVendor();
        }
    }

    private void buildUI() {
        H2 title = new H2("Quotations");

        HorizontalLayout headerLayout = new HorizontalLayout(title);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);

        statusField.setItems(Status.DRAFT,Status.APPROVED, Status.WAITING_APPROVAL, Status.REJECTED, Status.CANCELLED);
        statusField.setItemLabelGenerator(Status::getDisplayName);

        statusField.setClearButtonVisible(true);

        Button searchBtn = new Button("Search", e -> applyFilter());
        Button clearBtn = new Button("Clear", e -> clearFilter());

        filterBar = new HorizontalLayout(quoteIdField, rfqIdField);
        if (!isVendorUser) {
            filterBar.add(supplierField);
        }
        filterBar.add(statusField, searchBtn, clearBtn);
        filterBar.setAlignItems(Alignment.END);

        quotationGrid.addColumn(QuotationDTO::getId).setHeader("Quote ID").setWidth("100px").setFlexGrow(0);
        
        quotationGrid.addColumn(q -> q.getRequestForQuotation() != null ? "RFQ-" + q.getRequestForQuotation().getId() : "-")
                .setHeader("Source RFQ ID").setAutoWidth(true);
        
        if (!isVendorUser) {
            quotationGrid.addColumn(q -> q.getVendor() != null ? q.getVendor().getVendorName() : "-")
                    .setHeader("Supplier Name").setAutoWidth(true);
        }

        mapStatusBadgeColumn();

        quotationGrid.addColumn(q -> q.getQuotationDate() != null ? q.getQuotationDate().toString() : "-")
                .setHeader("Submission Date").setAutoWidth(true);
        
        quotationGrid.addColumn(q -> String.format("%.2f INR", q.getTotalAmount())).setHeader("Total Cost Offer").setAutoWidth(true);

        quotationGrid.setWidthFull();
        quotationGrid.setHeightFull();
        quotationGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        
        quotationGrid.addItemDoubleClickListener(event -> {
            QuotationDTO q = event.getItem();
            getUI().ifPresent(ui -> ui.navigate("quotation-details/" + q.getId()));
        });

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
        pageSizeField.setWidth("100px");
        pageSizeField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                pageSize = event.getValue();
                currentPage = 0;
                loadData();
            }
        });

        paginationLayout.add(prev, pageInfo, next, new Span("Page Size"), pageSizeField);
        paginationLayout.setWidthFull();
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setAlignItems(Alignment.CENTER);

        add(headerLayout, filterBar, quotationGrid, paginationLayout);
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
                badge.getStyle().set("background-color", "#dcfce7").set("color", "#15803d");
            } else if (q.getStatus() == Status.REJECTED) {
                badge.getStyle().set("background-color", "#fee2e2").set("color", "#b91c1c");
            } else {
                badge.getStyle().set("background-color", "#fef9c3").set("color", "#a16207");
            }
            return badge;
        }).setHeader("Status").setWidth("130px").setFlexGrow(0);
    }

    private void configureDefaultStatusFilters() {
        if (securityService.canAccessView("quotation-form")) {
            statusField.setValue(Status.DRAFT);
            qFilter.setStatus(Status.DRAFT);
        } else {
            statusField.clear();
            qFilter.setStatus(null);
        }
    }

    private void loadData() {
        if (qFilter == null) {
            qFilter = new QuotationDTO();
        }

        if (isVendorUser) {
            qFilter.setVendor(loggedInVendor);
            if (statusField.getValue() != null) {
                qFilter.setStatus(statusField.getValue());
            }
        } else {
            
            if (statusField.getValue() == null) {
                qFilter.setStatus(null); 
            } else {
                qFilter.setStatus(statusField.getValue());
            }
        }

        Page<QuotationDTO> page = quotationService.getAllQuotations(qFilter, currentPage, pageSize);

        quotationGrid.setItems(page.getContent());
        this.totalPages = page.getTotalPages() > 0 ? page.getTotalPages() : 1;
        pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
    }

    private void applyFilter() {
        qFilter = new QuotationDTO();
        
        if (!quoteIdField.isEmpty()) {
            qFilter.setId(Long.valueOf(quoteIdField.getValue().trim()));
        }
        if (!rfqIdField.isEmpty()) {
            RequestForQuotation rfq = new RequestForQuotation();
            rfq.setId(Long.valueOf(rfqIdField.getValue().trim()));
            qFilter.setRequestForQuotation(rfq);
        }
        
        if (isVendorUser) {
            qFilter.setVendor(loggedInVendor);
            qFilter.setStatus(statusField.getValue());
        } else {
            if (!supplierField.isEmpty()) {
                Vendor searchVendor = new Vendor();
                searchVendor.setVendorName(supplierField.getValue().trim());
                qFilter.setVendor(searchVendor);
            }
            qFilter.setStatus(statusField.getValue());
        }

        currentPage = 0;
        loadData();
    }

    private void clearFilter() {
        quoteIdField.clear();
        rfqIdField.clear();
        if (!isVendorUser) {
            supplierField.clear();
        }

        qFilter = new QuotationDTO();
        currentPage = 0;

        configureDefaultStatusFilters();
        loadData();
    }
}