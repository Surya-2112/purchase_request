package com.module.purchase.view.auditLogs;

import java.util.List;

import org.springframework.data.domain.Page;

import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;
import com.module.purchase.service.AuditLogsService;
import com.module.purchase.service.EmployeeService;
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
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "audit-logs", layout = MainLayout.class)
@PermitAll
public class AuditLogView extends VerticalLayout {

    private final AuditLogsService auditLogService;

    private final EmployeeService employeeService;

    private final Grid<AuditLogs> grid = new Grid<>(AuditLogs.class, false);

    private final TextField auditIdFilter = new TextField("Audit ID");

    private final ComboBox<EntityType> entityTypeFilter = new ComboBox<>("Entity Type");

    private final TextField entityIdFilter = new TextField("Entity ID");

    private final ComboBox<Action> actionFilter = new ComboBox<>("Action");

    private final ComboBox<Employee> performedByFilter = new ComboBox<Employee>("Performed By");

    private final DatePicker dateFilter = new DatePicker("Date");

    private int currentPage = 0;

    private int pageSize = 25;

    private int totalPage=0;

    private final Span pageInfo =new Span();

    private List<AuditLogs> currentFilter;

    private AuditLogs filteredLog;

    public AuditLogView( AuditLogsService auditLogService,EmployeeService employeeService) {

        this.auditLogService = auditLogService;
        this.employeeService = employeeService;

        setSizeFull();

        setPadding(true);

        setSpacing(true);

        H2 title = new H2("Audit Logs Details");

        entityTypeFilter.setItems(EntityType.values());

        actionFilter.setItems(Action.values());

        performedByFilter.setItems(employeeService.getEmployees());
        performedByFilter.setItemLabelGenerator(Employee::getEmployeeName);

        auditIdFilter.setWidth("80px");

        entityTypeFilter.setWidth("200px");

        entityIdFilter.setWidth("80px");

        actionFilter.setWidth("120px");

        performedByFilter.setWidth("180px");

        dateFilter.setWidth("150px");

        Button searchBtn =new Button("Search");

        searchBtn.addThemeVariants( ButtonVariant.LUMO_PRIMARY);

        Button clearBtn =new Button("Clear");

        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

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

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.setSizeFull();


        Button previousButton =new Button("Previous");

        Button nextButton =new Button("Next");

        ComboBox<Integer> pageSizeField =new ComboBox<>();

        pageSizeField.setItems(
                10,
                25,
                50,
                100);

        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(event -> {

            pageSize =event.getValue();

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

            if (currentPage <= totalPage) {

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

        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        paginationLayout.setAlignItems(Alignment.CENTER);

        applyFilters();

        add(title, filterLayout,grid,paginationLayout);

        expand(grid);
    }

    private void applyFilters() {
        
        filteredLog=new AuditLogs();
          
        Long id=null;
         if (!auditIdFilter.getValue().isEmpty()) {
            try {
                id = Long.valueOf(auditIdFilter.getValue().trim());
            } catch (NumberFormatException e) {
                id = -1L; 
            }
        }
        filteredLog.setAuditLogId(id);
        filteredLog.setEntityType(entityTypeFilter.getValue());
        id=null;
         if (!entityIdFilter.getValue().isEmpty()) {
            try {
                id = Long.valueOf(entityIdFilter.getValue().trim());
            } catch (NumberFormatException e) {
                id = -1L; 
            }
        }
        filteredLog.setEntityId(id);
        filteredLog.setAction(actionFilter.getValue());
        filteredLog.setPerformedBy(performedByFilter.getValue());
        filteredLog.setTimestamp(dateFilter.getValue());
        updateGrid();
    }

    private void updateGrid() {
        Page<AuditLogs> page=auditLogService.getAuditLogsHasPage(filteredLog,currentPage,pageSize);
        grid.setItems(page.getContent());
        totalPage=page.getTotalPages();
        pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPage );
    }
}