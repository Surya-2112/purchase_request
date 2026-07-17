package com.module.purchase.view.repeatedPeriod;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.enums.FrequencyType;
import com.module.purchase.enums.RepeatedPeriodReferType;
import com.module.purchase.service.PurchaseRequestLineService;
import com.module.purchase.service.RepeatedPeriodService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "repeated-periods", layout = MainLayout.class)
@PermitAll
public class RepeatedPeriodView extends VerticalLayout {

    private final RepeatedPeriodService repeatedPeriodService;
    private final PurchaseRequestLineService lineService;

    private final Grid<RepeatedPeriod> grid = new Grid<>(RepeatedPeriod.class, false);

    private int currentPage = 0;
    private int pageSize = 25;
    private int totalPages = 1;
    private final Span pageInfo = new Span();

    private final ComboBox<RepeatedPeriodReferType> referTypeFilter = new ComboBox<>("Reference Type");
    private final ComboBox<FrequencyType> frequencyTypeFilter = new ComboBox<>("Frequency Basis");
    private final DatePicker nextDateFilter = new DatePicker("Next Run Date");

    public RepeatedPeriodView(RepeatedPeriodService repeatedPeriodService, PurchaseRequestLineService lineService) {
        this.repeatedPeriodService = repeatedPeriodService;
        this.lineService = lineService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        buildUI();
        loadData();
    }

    private void buildUI() {
        H2 title = new H2("Automated Replenishment Loops & Schedules");

        referTypeFilter.setItems(RepeatedPeriodReferType.values());
        referTypeFilter.setWidth("200px");
        referTypeFilter.setClearButtonVisible(true);

        frequencyTypeFilter.setItems(FrequencyType.values());
        frequencyTypeFilter.setWidth("200px");
        frequencyTypeFilter.setClearButtonVisible(true);

        nextDateFilter.setErrorMessage("Enter a valid date");

        Button searchBtn = new Button("Apply Filters", e -> {
            currentPage = 0;
            loadData();
        });
        searchBtn.addThemeName("primary");

        Button clearBtn = new Button("Clear All", e -> {
            referTypeFilter.clear();
            frequencyTypeFilter.clear();
            nextDateFilter.clear();
            currentPage = 0;
            loadData();
        });

        clearBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout filtersLayout = new HorizontalLayout(referTypeFilter, frequencyTypeFilter, nextDateFilter, searchBtn, clearBtn);
        filtersLayout.setAlignItems(Alignment.END);

        grid.removeAllColumns();
        grid.addColumn(RepeatedPeriod::getId).setHeader("Schedule ID").setWidth("110px").setFlexGrow(0);
        grid.addColumn(RepeatedPeriod::getReferType).setHeader("Module Type").setAutoWidth(true);
        grid.addColumn(RepeatedPeriod::getReferId).setHeader("Source Row ID").setWidth("120px").setFlexGrow(0);

        grid.addColumn(period -> {
            if (period.getReferType() == RepeatedPeriodReferType.PURCHASE_REQUEST_LINE && period.getReferId() != null) {
                return lineService.getPurchaseRequestLineById(period.getReferId())
                        .map(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null 
                                ? line.getItemVariant().getItem().getItemName() : "No Item Metadata")
                        .orElse("Line Row Missing");
            }
            return "N/A";
        }).setHeader("Target Sourcing Item").setAutoWidth(true);

        grid.addColumn(period -> "Every " + period.getFrequencyPeriod() + " " + 
                (period.getFrequencyType() != null ? period.getFrequencyType().name() : ""))
                .setHeader("Recurrence Pattern").setAutoWidth(true);

        grid.addColumn(p -> p.getFromDate() != null ? p.getFromDate().toString() : "-").setHeader("Start Date").setWidth("130px");
        grid.addColumn(p -> p.getNextDate() != null ? p.getNextDate().toString() : "Pending...").setHeader("Next Run Target").setWidth("150px");

        grid.setWidthFull();
        grid.setHeightFull();
        grid.getStyle().set("border-radius", "12px").set("overflow", "hidden");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addItemDoubleClickListener(event -> {
            RepeatedPeriod selected = event.getItem();
            getUI().ifPresent(ui -> ui.navigate("repeated-period-details/" + selected.getId()));
        });

        Button prev = new Button("Previous");
        prev.addClickListener(event -> {
            if (currentPage > 0) {
                currentPage--;
                loadData();
            }
        });
        prev.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button next = new Button("Next", event -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadData();
            }
        });
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        ComboBox<Integer> pageSizeField = new ComboBox<>();
        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(pageSize);
        pageSizeField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                pageSize = event.getValue();
                currentPage = 0;
                loadData();
            }
        });

        HorizontalLayout paginationLayout = new HorizontalLayout(prev, pageInfo, next, new Span("Page Size"), pageSizeField);
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setAlignItems(Alignment.CENTER);
        paginationLayout.setWidthFull();

        add(title, filtersLayout, grid, paginationLayout);
        expand(grid);
    }

    private void loadData() {
        Pageable pageable = PageRequest.of(currentPage, pageSize);
        
        RepeatedPeriod criteriaFilter = new RepeatedPeriod();
        criteriaFilter.setReferType(referTypeFilter.getValue());
        criteriaFilter.setFrequencyType(frequencyTypeFilter.getValue());
        criteriaFilter.setNextDate(nextDateFilter.getValue());

        Page<RepeatedPeriod> page = repeatedPeriodService.getAllRepeatedPeriodsPaged(criteriaFilter, pageable);

        if (page != null) {
            grid.setItems(page.getContent());
            this.totalPages = page.getTotalPages() > 0 ? page.getTotalPages() : 1;
            pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
        }
    }
}