package com.module.purchase.view.unit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Unit;
import com.module.purchase.service.UnitService;
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

@Route(value = "unit", layout = MainLayout.class)
@PermitAll
public class UnitView extends VerticalLayout {

    private final UnitService unitService;

    private final Grid<Unit> unitGrid = new Grid<>(Unit.class, false);

    private final TextField unitIdField = new TextField("Unit ID");
    private final TextField unitNameField = new TextField("Unit Name");
    private final TextField unitCodeField = new TextField("Unit Code");

    private int currentPage = 0;
    private int pageSize = 25;
    private int totalPages = 1;

    private final Span pageInfo = new Span();

    private final Button previousButton = new Button("Previous");
    private final Button nextButton = new Button("Next");
    private Unit currentFilter = new Unit();

    public UnitView(UnitService unitService, SecurityService securityService) {
        this.unitService = unitService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Unit Master");

        Button addButton = new Button("Add Unit", event -> {
            UnitForm form = new UnitForm(unitService, securityService);
            form.addDetachListener(detachEvent -> loadUnits());
            form.open();
        });

        addButton.setVisible(securityService.canAccessView("unit-form"));

        HorizontalLayout headerLayout = new HorizontalLayout(title, addButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Button searchButton = new Button("Search", e -> applyFilter());
        Button clearButton = new Button("Clear", e -> clearFilter());

        HorizontalLayout filterLayout = new HorizontalLayout(
                unitIdField,
                unitNameField,
                unitCodeField,
                searchButton,
                clearButton);
        filterLayout.setWidthFull();
        filterLayout.setAlignItems(Alignment.END);

        unitGrid.addColumn(Unit::getId).setHeader("Unit ID").setAutoWidth(true);
        unitGrid.addColumn(Unit::getName).setHeader("Unit Name").setAutoWidth(true);
        unitGrid.addColumn(Unit::getCode).setHeader("Unit Code").setAutoWidth(true);

        unitGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        unitGrid.setSizeFull();

        unitGrid.addItemDoubleClickListener(event -> {
            Unit unit = event.getItem();
            getUI().ifPresent(ui -> ui.navigate("unit-details/" + unit.getId()));
        });

        ComboBox<Integer> pageSizeField = new ComboBox<>();
        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                pageSize = e.getValue();
                currentPage = 0;
                loadUnits();
            }
        });

        Button previousButton = new Button("Previous", e -> {
            if (currentPage > 0) {
                currentPage--;
                loadUnits();
            }
        });

        Button nextButton = new Button("Next", e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadUnits();
            }
        });

        HorizontalLayout paginationLayout = new HorizontalLayout(
                previousButton,
                pageInfo,
                nextButton,
                new Span("Page Size"),
                pageSizeField);
        paginationLayout.setWidthFull();
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setAlignItems(Alignment.CENTER);
       
        loadUnits();

        add(headerLayout, filterLayout, unitGrid, paginationLayout);
        expand(unitGrid);
    }

    private void loadUnits() {
        Page<Unit> page = unitService.getUnits(
                currentFilter,
                PageRequest.of(currentPage, pageSize));

        unitGrid.setItems(page.getContent());

        this.totalPages = page.getTotalPages() > 0 ? page.getTotalPages() : 1;

        pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
    }

    private void applyFilter() {
        Integer id = null;

        if (!unitIdField.getValue().isEmpty()) {
            try {
                id = Integer.valueOf(unitIdField.getValue().trim());
            } catch (NumberFormatException e) {
                id = -1;
            }
        }

        currentFilter = new Unit();
        currentFilter.setId(id);
        currentFilter.setName(unitNameField.isEmpty() ? null : unitNameField.getValue().trim());
        currentFilter.setCode(unitCodeField.isEmpty() ? null : unitCodeField.getValue().trim());

        currentPage = 0;
        loadUnits();
    }

    private void clearFilter() {
        unitIdField.clear();
        unitNameField.clear();
        unitCodeField.clear();

        currentFilter = new Unit();
        currentPage = 0;

        loadUnits();
    }
}