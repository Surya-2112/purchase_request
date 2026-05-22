package com.module.purchase.view.departmentBudget;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Department;
import com.module.purchase.entityDTO.DepartmentBudgetDTO;
import com.module.purchase.service.DepartmentBudgetService;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "department-budget", layout = MainLayout.class)
@PermitAll
public class DepartmentBudgetView extends VerticalLayout {

        private final DepartmentBudgetService departmentBudgetService;

        private final DepartmentService departmentService;

      //  private final SecurityService securityService;

        private final Grid<DepartmentBudgetDTO> departmentBudgetGrid = new Grid<>(DepartmentBudgetDTO.class, false);

        private final TextField departmentBudgetIdField = new TextField("Department Budget ID");

        private final ComboBox<Department> departmentField = new ComboBox<>("Department");

        private final ComboBox<Year> yearField = new ComboBox<>("Year");

        // PAGINATION
        private int currentPage = 0;

        private int pageSize = 25;

        private final Span pageInfo = new Span();

        private DepartmentBudgetDTO currentFilter = new DepartmentBudgetDTO();

        public DepartmentBudgetView(SecurityService securityService,
                        DepartmentBudgetService departmentBudgetService,
                        DepartmentService departmentServices) {

                this.departmentBudgetService = departmentBudgetService;

                this.departmentService = departmentServices;

             //   this.securityService=securityService;

                setSizeFull();

                setPadding(true);

                setSpacing(true);

                // LOAD DEPARTMENTS
                departmentField.setItems(
                                departmentService.getDepartments());

                departmentField.setItemLabelGenerator(
                                department -> department.getDepartmentName());

                // LOAD YEARS
                List<Year> years = new ArrayList<>();

                for (int year = 2000; year <= 2100; year++) {

                        years.add(Year.of(year));
                }

                yearField.setItems(years);

                yearField.setItemLabelGenerator(
                                year -> String.valueOf(year.getValue()));

                // HEADER
                HorizontalLayout headerLayout = new HorizontalLayout();

                H2 title = new H2("Department Budget List");

                Button addButton = new Button("Add Department Budget");

                addButton.addClickListener(event -> {

                        DepartmentBudgetForm form = new DepartmentBudgetForm(
                                        departmentBudgetService,
                                        departmentService, securityService);

                        form.open();
                });

                headerLayout.add(title, addButton);

                headerLayout.setWidthFull();

                headerLayout.setJustifyContentMode(
                                JustifyContentMode.BETWEEN);

                headerLayout.setAlignItems(
                                Alignment.CENTER);

                // FILTER
                HorizontalLayout filterLayout = new HorizontalLayout();

                Button searchButton = new Button(
                                "Search",
                                event -> applyFilter());

                Button clearButton = new Button(
                                "Clear",
                                event -> clearFilter());

                filterLayout.add(
                                departmentBudgetIdField,
                                departmentField,
                                yearField,
                                searchButton,
                                clearButton);

                filterLayout.setAlignItems(
                                Alignment.END);

                filterLayout.setWidthFull();

                // GRID
                departmentBudgetGrid.addComponentColumn(departmentBudget -> {
                        Button idButton = new Button(String.valueOf(departmentBudget.getDepartmentBudgetId()));
                        idButton.addClickListener(event -> {

                                getUI().ifPresent(ui -> ui.navigate("department-budget-details/"
                                                + departmentBudget.getDepartmentBudgetId()));
                        });
                        return idButton;
                })
                                .setHeader("Budget ID")
                                .setAutoWidth(true);

                departmentBudgetGrid.addColumn(departmentBudget -> departmentBudget.getDepartment() == null
                                ? ""
                                : departmentBudget
                                                .getDepartment()
                                                .getDepartmentName())

                                .setHeader("Department")
                                .setAutoWidth(true);

                departmentBudgetGrid.addColumn(
                                departmentBudget ->

                                departmentBudget.getYear() == null
                                                ? ""
                                                : departmentBudget
                                                                .getYear()
                                                                .toString())

                                .setHeader("Year")
                                .setAutoWidth(true);

                departmentBudgetGrid.addThemeVariants(
                                GridVariant.LUMO_ROW_STRIPES);

                departmentBudgetGrid.setSizeFull();

                departmentBudgetGrid.addItemClickListener(event -> {

                        DepartmentBudgetDTO departmentBudget = event.getItem();

                        Notification.show("Department : " + (departmentBudget.getDepartment() == null ? ""
                                        : departmentBudget.getDepartment().getDepartmentName()),
                                        3000,
                                        Notification.Position.TOP_CENTER);

                });

                // PAGINATION
                Button previousButton = new Button("Previous");

                Button nextButton = new Button("Next");
                ComboBox<Integer> pageSizeField = new ComboBox<>();
                pageSizeField.setItems(10, 25, 50, 100);
                pageSizeField.setValue(25);

                pageSizeField.addValueChangeListener(e -> {
                        pageSize = e.getValue();
                        currentPage = 0;
                        loadDepartmentBudgets();
                });

                previousButton.addClickListener(event -> {

                        if (currentPage > 0) {

                                currentPage--;

                                loadDepartmentBudgets();
                        }
                });

                nextButton.addClickListener(event -> {

                        currentPage++;

                        loadDepartmentBudgets();
                });

                HorizontalLayout paginationLayout =
                 new HorizontalLayout(previousButton, pageInfo, nextButton,  new Span("Page Size"), pageSizeField);

                paginationLayout.setWidthFull();

                paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);

                paginationLayout.setAlignItems(Alignment.CENTER);

                // LOAD DATA
                loadDepartmentBudgets();

                add(headerLayout, filterLayout, departmentBudgetGrid, paginationLayout);

                expand(departmentBudgetGrid);
        }

        private void loadDepartmentBudgets() {

                Page<DepartmentBudgetDTO> departmentBudgetPage = departmentBudgetService.getAllDepartmentBudgets(
                                currentFilter, currentPage, pageSize);

                departmentBudgetGrid.setItems(departmentBudgetPage.getContent());

                pageInfo.setText("Page " + (currentPage + 1) + " of " + departmentBudgetPage.getTotalPages());
        }

        private void applyFilter() {

                Long departmentBudgetId = null;

                if (!departmentBudgetIdField.getValue().isEmpty()) {
                        departmentBudgetId = Long.valueOf(departmentBudgetIdField.getValue().trim());
                }

                currentFilter = new DepartmentBudgetDTO();
                currentFilter.setDepartmentBudgetId(departmentBudgetId);
                currentFilter.setDepartment(departmentField.getValue());
                currentFilter.setYear(yearField.getValue());
                currentPage = 0;

                loadDepartmentBudgets();
        }

        private void clearFilter() {

                departmentBudgetIdField.clear();

                departmentField.clear();

                yearField.clear();

                currentFilter = new DepartmentBudgetDTO();

                currentPage = 0;

                loadDepartmentBudgets();
        }
}