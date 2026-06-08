package com.module.purchase.view.unit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Unit;
import com.module.purchase.service.UnitService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
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
    private final SecurityService securityService;

    private final Grid<Unit> unitGrid = new Grid<>(Unit.class, false);

    private final TextField unitIdField = new TextField("Unit ID");
    private final TextField unitNameField = new TextField("Unit Name");
    private final TextField unitCodeField = new TextField("Unit Code");

    private int currentPage = 0;
    private int pageSize = 25;

    private final Span pageInfo = new Span();

    public UnitView(UnitService unitService, SecurityService securityService) {

        this.unitService = unitService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // HEADER
        H2 title = new H2("Unit Master");

        Button addButton = new Button("Add Unit", e -> {
            UnitForm form = new UnitForm(unitService, securityService);
            form.open();
        });

        addButton.setVisible(securityService.canAccessView("unit-form"));

        HorizontalLayout header = new HorizontalLayout(title, addButton);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // FILTER BUTTONS
        Button searchButton = new Button("Search", e -> loadUnits());
        Button clearButton = new Button("Clear", e -> clearFilters());

        HorizontalLayout filterLayout = new HorizontalLayout(
                unitIdField,
                unitNameField,
                unitCodeField,
                searchButton,
                clearButton
        );

        filterLayout.setAlignItems(Alignment.END);
        filterLayout.setWidthFull();

        // GRID CONFIG
        unitGrid.addColumn(Unit::getId)
                .setHeader("ID")
                .setAutoWidth(true);

        unitGrid.addColumn(Unit::getName)
                .setHeader("Unit Name")
                .setAutoWidth(true);

        unitGrid.addColumn(Unit::getCode)
                .setHeader("Code")
                .setAutoWidth(true);

        unitGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        unitGrid.setSizeFull();

        // DOUBLE CLICK (optional edit page)
        unitGrid.addItemDoubleClickListener(event -> {
            Unit unit = event.getItem();
            getUI().ifPresent(ui ->
                    ui.navigate("unit-edit/" + unit.getId())
            );
        });

        // PAGINATION
        Button prev = new Button("Previous", e -> {
            if (currentPage > 0) {
                currentPage--;
                loadUnits();
            }
        });

        Button next = new Button("Next", e -> {
            currentPage++;
            loadUnits();
        });

        HorizontalLayout pagination = new HorizontalLayout(prev, pageInfo, next);
        pagination.setAlignItems(Alignment.CENTER);

        // LOAD
        loadUnits();

        add(header, filterLayout, unitGrid, pagination);
        expand(unitGrid);
    }

    private void loadUnits() {
        Unit unit=new Unit();
        unit.setId(unitIdField.getValue().isBlank() ? null : Integer.parseInt(unitIdField.getValue()));
        unit.setName(unitNameField.getValue());
        unit.setCode(unitCodeField.getValue());
        

        Page<Unit> page = unitService.getUnits(unit,PageRequest.of(currentPage, pageSize)
        );

        unitGrid.setItems(page.getContent());

        pageInfo.setText("Page " + (currentPage + 1)
                + " of " + page.getTotalPages());
    }

    private void clearFilters() {
        unitIdField.clear();
        unitNameField.clear();
        unitCodeField.clear();

        currentPage = 0;
        loadUnits();
    }
}