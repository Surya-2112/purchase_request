package com.module.purchase.view.auditLogs;

import java.time.LocalDate;
import java.util.List;

import com.module.purchase.entity.AuditLogs;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;
import com.module.purchase.service.AuditLogsService;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "audit-logs", layout = MainLayout.class)
@PermitAll
public class AuditLogView extends VerticalLayout {

    private final AuditLogsService auditLogService;

    private final Grid<AuditLogs> grid =
            new Grid<>(AuditLogs.class, false);

    private final TextField auditIdFilter =
            new TextField("Audit ID");

    private final ComboBox<EntityType> entityTypeFilter =
            new ComboBox<>("Entity Type");

    private final TextField entityIdFilter =
            new TextField("Entity ID");

    private final ComboBox<Action> actionFilter =
            new ComboBox<>("Action");

    private final TextField performedByFilter =
            new TextField("Performed By");

    private final DatePicker dateFilter =
            new DatePicker("Date");

    private int currentPage = 0;

    private int pageSize = 25;

    private final Span pageInfo =new Span();

    private List<AuditLogs> filteredLogs;

    public AuditLogView( AuditLogsService auditLogService) {

        this.auditLogService = auditLogService;

        setSizeFull();

        setPadding(true);

        setSpacing(true);

        // ================= HEADER =================

        H2 title = new H2("Audit Logs Details");

        // ================= FILTER VALUES =================

        entityTypeFilter.setItems(
                EntityType.values());

        actionFilter.setItems(
                Action.values());

        auditIdFilter.setWidth("80px");

        entityTypeFilter.setWidth("200px");

        entityIdFilter.setWidth("80px");

        actionFilter.setWidth("120px");

        performedByFilter.setWidth("180px");

        dateFilter.setWidth("150px");

        auditIdFilter.setValueChangeMode(
                ValueChangeMode.EAGER);

        entityIdFilter.setValueChangeMode(
                ValueChangeMode.EAGER);

        performedByFilter.setValueChangeMode(
                ValueChangeMode.EAGER);

        // ================= BUTTONS =================

        Button searchBtn =
                new Button("Search");

        searchBtn.addThemeVariants(
                ButtonVariant.LUMO_PRIMARY);

        Button clearBtn =
                new Button("Clear");

        clearBtn.addThemeVariants(
                ButtonVariant.LUMO_TERTIARY);

        searchBtn.addClickListener(event -> {

            currentPage = 0;

            applyFilters();
        });

        clearBtn.addClickListener(event -> {

            auditIdFilter.clear();

            entityTypeFilter.clear();

            entityIdFilter.clear();

            actionFilter.clear();

            performedByFilter.clear();

            dateFilter.clear();

            currentPage = 0;

            applyFilters();
        });

        // ================= FILTER LAYOUT =================

        HorizontalLayout filterLayout =
                new HorizontalLayout(
                        auditIdFilter,
                        entityTypeFilter,
                        entityIdFilter,
                        actionFilter,
                        performedByFilter,
                        dateFilter,
                        searchBtn,
                        clearBtn);

        filterLayout.setWidthFull();

        filterLayout.setAlignItems(
                Alignment.END);

        // ================= GRID =================

        grid.addColumn(AuditLogs::getAuditLogId)
                .setHeader("Audit ID")
                .setAutoWidth(true);

        grid.addColumn(log ->
                log.getEntityType() == null
                        ? ""
                        : log.getEntityType().name())
                .setHeader("Entity Type")
                .setAutoWidth(true);

        grid.addColumn(AuditLogs::getEntityId)
                .setHeader("Entity ID")
                .setAutoWidth(true);

        grid.addColumn(log ->
                log.getAction() == null
                        ? ""
                        : log.getAction().name())
                .setHeader("Action")
                .setAutoWidth(true);

        grid.addColumn(log ->
                log.getPerformedBy() == null
                        ? ""
                        : log.getPerformedBy().getEmployeeName())
                .setHeader("Performed By")
                .setAutoWidth(true);

        grid.addColumn(AuditLogs::getTimestamp)
                .setHeader("Timestamp")
                .setAutoWidth(true);

        grid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES);

        grid.setSizeFull();

        // ================= PAGINATION =================

        Button previousButton =
                new Button("Previous");

        Button nextButton =
                new Button("Next");

        ComboBox<Integer> pageSizeField =
                new ComboBox<>();

        pageSizeField.setItems(
                10,
                25,
                50,
                100);

        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(event -> {

            pageSize =
                    event.getValue();

            currentPage = 0;

            updateGrid();
        });

        previousButton.addClickListener(event -> {

            if (currentPage > 0) {

                currentPage--;

                updateGrid();
            }
        });

        nextButton.addClickListener(event -> {

            int totalPages =
                    (int) Math.ceil(
                            (double) filteredLogs.size()
                                    / pageSize);

            if (currentPage < totalPages - 1) {

                currentPage++;

                updateGrid();
            }
        });

        HorizontalLayout paginationLayout =
                new HorizontalLayout(
                        previousButton,
                        pageInfo,
                        nextButton,
                        new Span("Page Size"),
                        pageSizeField);

        paginationLayout.setWidthFull();

        paginationLayout.setJustifyContentMode(
                JustifyContentMode.CENTER);

        paginationLayout.setAlignItems(
                Alignment.CENTER);

        // ================= INITIAL LOAD =================

        applyFilters();

        // ================= ADD COMPONENTS =================

        add(
                title,
                filterLayout,
                grid,
                paginationLayout);

        expand(grid);
    }

    private void applyFilters() {

        filteredLogs =
                auditLogService.getAllAuditLogs()
                        .stream()
                        .filter(log -> {

                            boolean matchesAuditId =
                                    auditIdFilter.isEmpty()
                                            || String.valueOf(
                                                    log.getAuditLogId())
                                                    .contains(
                                                            auditIdFilter.getValue());

                            boolean matchesEntityType =
                                    entityTypeFilter.isEmpty()
                                            || log.getEntityType()
                                                    == entityTypeFilter.getValue();

                            boolean matchesEntityId =
                                    entityIdFilter.isEmpty()
                                            || String.valueOf(
                                                    log.getEntityId())
                                                    .contains(
                                                            entityIdFilter.getValue());

                            boolean matchesAction =
                                    actionFilter.isEmpty()
                                            || log.getAction()
                                                    == actionFilter.getValue();

                            boolean matchesPerformedBy =
                                    performedByFilter.isEmpty()
                                            || (log.getPerformedBy() != null
                                                    && log.getPerformedBy()
                                                            .getEmployeeName()
                                                            .toLowerCase()
                                                            .contains(
                                                                    performedByFilter.getValue()
                                                                            .toLowerCase()));

                            boolean matchesDate = true;

                            if (dateFilter.getValue() != null
                                    && log.getTimestamp() != null) {

                                LocalDate logDate =
                                        log.getTimestamp();

                                matchesDate =
                                        logDate.equals(
                                                dateFilter.getValue());
                            }

                            return matchesAuditId
                                    && matchesEntityType
                                    && matchesEntityId
                                    && matchesAction
                                    && matchesPerformedBy
                                    && matchesDate;

                        })
                        .toList();

        updateGrid();
    }

    private void updateGrid() {

        int start =
                currentPage * pageSize;

        int end =
                Math.min(
                        start + pageSize,
                        filteredLogs.size());

        if (start > end) {

            currentPage = 0;

            start = 0;

            end =
                    Math.min(
                            pageSize,
                            filteredLogs.size());
        }

        grid.setItems(
                filteredLogs.subList(start, end));

        int totalPages =(int) Math.ceil((double) filteredLogs.size()
                                / pageSize);

        pageInfo.setText(
                "Page "
                        + (currentPage + 1)
                        + " of "
                        + (totalPages == 0 ? 1 : totalPages));
    }
}