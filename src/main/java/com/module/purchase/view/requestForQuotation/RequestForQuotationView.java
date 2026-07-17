package com.module.purchase.view.requestForQuotation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entityDTO.QuotationDTO;
import com.module.purchase.enums.RequestForQuotationStatus;
import com.module.purchase.enums.Status;
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

    private Boolean isVendor = false;

    private final Grid<RequestForQuotation> rfqGrid = new Grid<>(RequestForQuotation.class, false);

    private int currentPage = 0;
    private int pageSize = 25;
    private int totalPages = 1;
    private final Span pageInfo = new Span();

    private final TextField rfqIdField = new TextField("RFQ ID");
    private final ComboBox<RequestForQuotationStatus> statusFilter = new ComboBox<>("Status");
    private final DatePicker requestedDateFilter = new DatePicker("Requested Date");

    public RequestForQuotationView(RequestForQuotationService rfqService, SecurityService securityService,
            QuotationService quotationService) {
        this.rfqService = rfqService;
        this.securityService = securityService;
        this.quotationService = quotationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        isVendor = !(securityService.getLoggedInUser().getVendor() == null);

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

        statusFilter.setItems(RequestForQuotationStatus.values());
        statusFilter.setValue(RequestForQuotationStatus.DRAFT);
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("200px");

        if (isVendor) {
            statusFilter.setVisible(false);
            statusFilter.setValue(RequestForQuotationStatus.OPEN);
        }

        rfqIdField.setPlaceholder("Search ID...");
        rfqIdField.setWidth("150px");
        rfqIdField.setPattern("[0-9]{0,20}");
        rfqIdField.setErrorMessage("Enter a valid nuumber");

        requestedDateFilter.setErrorMessage("Enter a valid Date");

        Button search = new Button("Search", e -> {
            currentPage = 0;
            loadData();
        });
        search.addThemeName("primary");

        Button clear = new Button("Clear", e -> {
            rfqIdField.clear();
            statusFilter.setValue(RequestForQuotationStatus.DRAFT);
            if (isVendor) { statusFilter.setValue(RequestForQuotationStatus.OPEN); }
            requestedDateFilter.clear();
            currentPage = 0;
            loadData();
        });

        HorizontalLayout filtersLayout = new HorizontalLayout(rfqIdField, statusFilter, requestedDateFilter, search, clear);
        filtersLayout.setAlignItems(Alignment.END);
        filtersLayout.setSpacing(true);

        rfqGrid.removeAllColumns();

        rfqGrid.addColumn(RequestForQuotation::getId).setHeader("RFQ ID").setWidth("110px").setFlexGrow(0);
        rfqGrid.addColumn(rfq -> rfq.getRequestedDate() != null ? rfq.getRequestedDate().toString() : "-")
                .setHeader("Requested Date").setAutoWidth(true);
        rfqGrid.addColumn(rfq -> rfq.getRequestEndDate() != null ? rfq.getRequestEndDate().toString() : "-")
                .setHeader("Closing/End Date").setAutoWidth(true);

        rfqGrid.addColumn(rfq -> {
            if (rfq.getStatus() == RequestForQuotationStatus.DRAFT) {
                return "-";
            }
            QuotationDTO quotation=new QuotationDTO();
            quotation.setRequestForQuotation(rfq);
            if(rfq.getStatus() == RequestForQuotationStatus.OPEN)
            {
              quotation.setStatus(Status.WAITING_APPROVAL);
            }
            if(rfq.getStatus() == RequestForQuotationStatus.CLOSED)
            {
                quotation.setStatus(Status.REJECTED);
            }
            return String.valueOf(rfq.getStatus() == RequestForQuotationStatus.CLOSED ? quotationService.getCountQuotations(quotation)+1:quotationService.getCountQuotations(quotation));
        })
        .setHeader("Quotations Recv").setWidth("150px");

        gridStatusBadgeMapping();

        rfqGrid.setWidthFull();
        rfqGrid.setHeightFull();
        rfqGrid.getStyle().set("border-radius", "12px").set("overflow", "hidden");
        rfqGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        rfqGrid.addItemDoubleClickListener(event -> {
            RequestForQuotation selectedRfq = event.getItem();
            getUI().ifPresent(ui -> ui.navigate("request-for-quotation-details/" + selectedRfq.getId()));
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
        pageSizeField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                pageSize = event.getValue();
                currentPage = 0;
                loadData();
            }
        });

        HorizontalLayout paginationLayout = new HorizontalLayout(prev, pageInfo, next, new Span("Page Size"),
                pageSizeField);
        paginationLayout.setWidthFull();
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setAlignItems(Alignment.CENTER);

        add(headerLayout, filtersLayout, rfqGrid, paginationLayout);
        expand(rfqGrid);
    }

    private void gridStatusBadgeMapping() {
        rfqGrid.addComponentColumn(rfq -> {
            Span badge = new Span(rfq.getStatus() != null ? rfq.getStatus().name() : "UNKNOWN");

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
            }
        }
        filterCriteria.setStatus(statusFilter.getValue());
        filterCriteria.setRequestedDate(requestedDateFilter.getValue());

        Page<RequestForQuotation> page = rfqService.getRequestsForQuotationPaged(filterCriteria,securityService.getLoggedInUser().getVendor(), pageable);

        if (page != null) {
            rfqGrid.setItems(page.getContent());
            this.totalPages = page.getTotalPages() > 0 ? page.getTotalPages() : 1;
            pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
        }
    }
}