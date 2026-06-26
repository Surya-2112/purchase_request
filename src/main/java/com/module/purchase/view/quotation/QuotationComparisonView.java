package com.module.purchase.view.quotation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entityDTO.QuotationDTO;
import com.module.purchase.enums.RequestForQuotationStatus;
import com.module.purchase.enums.Status;
import com.module.purchase.service.QuotationService;
import com.module.purchase.service.RequestForQuotationService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "quotation-comparison", layout = MainLayout.class)
@PermitAll
public class QuotationComparisonView extends VerticalLayout {

    private final RequestForQuotationService rfqService;
    private final QuotationService quotationService;

    private final TextField filterUnassignedRfqId = new TextField();
    private final DatePicker filterUnassignedDate = new DatePicker();
    private final TextField filterUnassignedQuoteCount = new TextField();

    private final TextField filterAssignedRfqId = new TextField();
    private final DatePicker filterAssignedDate = new DatePicker();
    private final TextField filterAssignedQuoteCount = new TextField();

    private final Grid<RequestForQuotation> unassignedGrid = new Grid<>(RequestForQuotation.class, false);
    private final Grid<RequestForQuotation> assignedGrid = new Grid<>(RequestForQuotation.class, false);

    private final List<RequestForQuotation> masterUnassignedList = new ArrayList<>();
    private final List<RequestForQuotation> masterAssignedList = new ArrayList<>();

    public QuotationComparisonView(RequestForQuotationService rfqService, QuotationService quotationService) {
        this.rfqService = rfqService;
        this.quotationService = quotationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 viewTitle = new H2("Quotations Evaluation & Comparison");

        configureGridsBaseLayouts();
        buildLiveFilteringBars();

        Tab unassignedTab = new Tab("Pending Evaluation ");
        Tab assignedTab = new Tab("Finalized Quotations ");
        Tabs navigationTabs = new Tabs(unassignedTab, assignedTab);
        navigationTabs.setWidthFull();

        VerticalLayout unassignedTabContent = new VerticalLayout(
        createFilterHeaderRow(filterUnassignedRfqId, filterUnassignedDate, filterUnassignedQuoteCount), unassignedGrid);
        unassignedTabContent.setSizeFull();
        unassignedTabContent.setPadding(false);

        VerticalLayout assignedTabContent = new VerticalLayout(createFilterHeaderRow(filterAssignedRfqId, filterAssignedDate, filterAssignedQuoteCount), assignedGrid);
        assignedTabContent.setSizeFull();
        assignedTabContent.setPadding(false);
        assignedTabContent.setVisible(false);

        navigationTabs.addSelectedChangeListener(event -> {
            boolean isUnassignedActive = event.getSelectedTab().equals(unassignedTab);
            unassignedTabContent.setVisible(isUnassignedActive);
            assignedTabContent.setVisible(!isUnassignedActive);
        });

        add(viewTitle, new Hr(), navigationTabs, unassignedTabContent, assignedTabContent);

        refreshWorkspaceDatasets();
    }

    private void configureGridsBaseLayouts() {

        setupGridColumnsTemplate(unassignedGrid);
        unassignedGrid.addComponentColumn(rfq -> {
            Button analyzeBtn = new Button("Compare Quotations", VaadinIcon.BAR_CHART.create());
            analyzeBtn.addThemeName("primary small");

            analyzeBtn.addClickListener(
                    e -> getUI().ifPresent(ui -> ui.navigate("quotation-evaluation-matrix/" + rfq.getId())));
            return analyzeBtn;
        }).setHeader("Evaluation Action").setAutoWidth(true);

        setupGridColumnsTemplate(assignedGrid);
        assignedGrid.addComponentColumn(rfq -> {
            Button reviewBtn = new Button("Review RFQ Records", VaadinIcon.EYE.create());
            reviewBtn.addThemeName("secondary small");
            reviewBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("rfq-finalized-view/" + rfq.getId())));
            return reviewBtn;
        }).setHeader("Review Action").setAutoWidth(true);
    }

    private void setupGridColumnsTemplate(Grid<RequestForQuotation> grid) {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setSizeFull();

        grid.addColumn(rfq -> "RFQ-" + rfq.getId()).setHeader("RFQ Reference ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(rfq -> rfq.getRequestedDate() != null ? rfq.getRequestedDate().toString() : "-")
                .setHeader("Requested Date").setAutoWidth(true).setSortable(true);
        grid.addColumn(rfq -> rfq.getRequestEndDate() != null ? rfq.getRequestEndDate().toString() : "-")
                .setHeader("Closing / End Date").setAutoWidth(true);

        grid.addColumn(rfq -> {
            QuotationDTO quotationDTO = new QuotationDTO();
            quotationDTO.setRequestForQuotation(rfq);
            if(rfq.getStatus().equals(RequestForQuotationStatus.OPEN))
                quotationDTO.setStatus(Status.WAITING_APPROVAL);
            else{
                quotationDTO.setStatus(null);
            }
            return String.valueOf(quotationService.getCountQuotations(quotationDTO));
        }).setHeader("Quotations Received").setAutoWidth(true).setSortable(true);
    }

    private HorizontalLayout createFilterHeaderRow(TextField idField, DatePicker dateField, TextField countField) {
        idField.setPlaceholder("Filter by RFQ ID...");
        idField.setValueChangeMode(ValueChangeMode.EAGER);
        idField.setClearButtonVisible(true);

        dateField.setPlaceholder("Filter by Date...");
        dateField.setClearButtonVisible(true);

        countField.setPlaceholder("Filter by Quote Count...");
        countField.setValueChangeMode(ValueChangeMode.EAGER);
        countField.setClearButtonVisible(true);

        HorizontalLayout bar = new HorizontalLayout(idField, dateField, countField);
        bar.setWidthFull();
        bar.setSpacing(true);
        return bar;
    }

    private void buildLiveFilteringBars() {
        filterUnassignedRfqId.addValueChangeListener(e -> executeUnassignedGridFilterPipeline());
        filterUnassignedDate.addValueChangeListener(e -> executeUnassignedGridFilterPipeline());
        filterUnassignedQuoteCount.addValueChangeListener(e -> executeUnassignedGridFilterPipeline());

        filterAssignedRfqId.addValueChangeListener(e -> executeAssignedGridFilterPipeline());
        filterAssignedDate.addValueChangeListener(e -> executeAssignedGridFilterPipeline());
        filterAssignedQuoteCount.addValueChangeListener(e -> executeAssignedGridFilterPipeline());
    }

    private void refreshWorkspaceDatasets() {
        masterUnassignedList.clear();
        masterAssignedList.clear();

        List<RequestForQuotation> closedRfqs = rfqService.getAllRequestsForQuotation().stream()
                .filter(rfq -> rfq.getStatus() == RequestForQuotationStatus.CLOSED)
                .toList();

        for (RequestForQuotation rfq : closedRfqs) {
            List<Quotation> associatedQuotes = quotationService.getQuotationsByRfq(rfq);

            boolean hasApprovedWinner = associatedQuotes.stream()
                    .anyMatch(quote -> quote.getStatus() == Status.APPROVED);

            if (hasApprovedWinner) {
                masterAssignedList.add(rfq);
            } else {
                masterUnassignedList.add(rfq);
            }
        }

        unassignedGrid.setItems(new ArrayList<>(masterUnassignedList));
        assignedGrid.setItems(new ArrayList<>(masterAssignedList));
    }

    private void executeUnassignedGridFilterPipeline() {
        String idSearch = filterUnassignedRfqId.getValue().trim().toLowerCase();
        LocalDate dateSearch = filterUnassignedDate.getValue();
        String countSearch = filterUnassignedQuoteCount.getValue().trim();

        List<RequestForQuotation> filtered = masterUnassignedList.stream().filter(rfq -> {
            boolean matchesId = idSearch.isEmpty() || String.valueOf(rfq.getId()).contains(idSearch)
                    || ("rfq-" + rfq.getId()).contains(idSearch);
            boolean matchesDate = dateSearch == null
                    || (rfq.getRequestedDate() != null && rfq.getRequestedDate().equals(dateSearch));

            int quotesReceivedCount = quotationService.getQuotationsByRfq(rfq).size();
            boolean matchesCount = countSearch.isEmpty() || String.valueOf(quotesReceivedCount).equals(countSearch);

            return matchesId && matchesDate && matchesCount;
        }).toList();

        unassignedGrid.setItems(filtered);
    }

    private void executeAssignedGridFilterPipeline() {
        String idSearch = filterAssignedRfqId.getValue().trim().toLowerCase();
        LocalDate dateSearch = filterAssignedDate.getValue();
        String countSearch = filterAssignedQuoteCount.getValue().trim();

        List<RequestForQuotation> filtered = masterAssignedList.stream().filter(rfq -> {
            boolean matchesId = idSearch.isEmpty() || String.valueOf(rfq.getId()).contains(idSearch)
                    || ("rfq-" + rfq.getId()).contains(idSearch);
            boolean matchesDate = dateSearch == null
                    || (rfq.getRequestedDate() != null && rfq.getRequestedDate().equals(dateSearch));

            int quotesReceivedCount = quotationService.getQuotationsByRfq(rfq).size();
            boolean matchesCount = countSearch.isEmpty() || String.valueOf(quotesReceivedCount).equals(countSearch);

            return matchesId && matchesDate && matchesCount;
        }).toList();

        assignedGrid.setItems(filtered);
    }
}