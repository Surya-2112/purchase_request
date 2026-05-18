package com.module.purchase.view.employee;

import org.springframework.data.domain.Page;

import com.module.purchase.entity.Department;
import com.module.purchase.entity.Role;
import com.module.purchase.entityDTO.EmployeeDTO;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.RoleService;
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

@Route(value = "employee", layout = MainLayout.class)
@PermitAll
public class EmployeeView extends VerticalLayout {

        private EmployeeService employeeService;
        private final Grid<EmployeeDTO> employeeGrid = new Grid<>(EmployeeDTO.class, false);

        private final TextField employeeIdField = new TextField("Employee ID");
        private final TextField employeeNameField = new TextField("Employee Name");

        private final ComboBox<Department> departmentField = new ComboBox<>("Department");
        private final ComboBox<Role> roleField = new ComboBox<>("Role");
        private final ComboBox<String> activeField = new ComboBox<>("Active");
        private int currentPage = 0;
        private int pageSize = 25;

        Span pageInfo = new Span();

        private EmployeeDTO currentFilter = new EmployeeDTO();

        public EmployeeView(EmployeeService employeeService, RoleService roleService,
                        DepartmentService departmentService) {
                this.employeeService = employeeService;
                setSizeFull();
                setPadding(true);
                setSpacing(true);

                departmentField.setItems(departmentService.getDepartments());

                roleField.setItems(
                                roleService.getRoles());

                departmentField.setItemLabelGenerator(
                                Department::getDepartmentName);

                roleField.setItemLabelGenerator(
                                Role::getRoleName);

                activeField.setItems("Yes", "No");

                Button previousButton = new Button("Previous");

                Button nextButton = new Button("Next");

                ComboBox<Integer> pageSizeField = new ComboBox<>();

                pageSizeField.setItems(10, 25, 50, 100);

                pageSizeField.setValue(25);

                pageSizeField.addValueChangeListener(event -> {

                        pageSize = event.getValue();

                        currentPage = 0;

                        loadEmployees();
                });

                previousButton.addClickListener(event -> {

                        if (currentPage > 0) {

                                currentPage--;

                                loadEmployees();
                        }
                });

                nextButton.addClickListener(event -> {

                        currentPage++;

                        loadEmployees();
                });

                HorizontalLayout paginationLayout = new HorizontalLayout(
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

                HorizontalLayout headerLayout = new HorizontalLayout();

                H2 title = new H2("Employees List");

                Button addButton = new Button("Add Employee");

                addButton.addClickListener(event -> {
                        EmployeeForm form = new EmployeeForm(
                                        employeeService,
                                        departmentService,
                                        roleService);

                        form.open();
                });

                headerLayout.add(title, addButton);
                headerLayout.setWidthFull();
                headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
                headerLayout.setAlignItems(Alignment.CENTER);

                // FILTER LAYOUT
                HorizontalLayout filterLayout = new HorizontalLayout();

                Button searchButton = new Button("Search", event -> applyFilter());

                Button clearButton = new Button("Clear", event -> clearFilter());

                filterLayout.setAlignItems(Alignment.END);

                filterLayout.add(
                                employeeIdField,
                                employeeNameField,
                                departmentField,
                                roleField,
                                activeField,
                                searchButton,
                                clearButton);

                filterLayout.setWidthFull();

                // GRID COLUMNS
                employeeGrid.addComponentColumn(employee -> {
                        Button employeeIdButton = new Button(String.valueOf(employee.getEmployeeId()));

                        employeeIdButton.addClickListener(event -> {

                                getUI().ifPresent(ui -> ui.navigate("employee-details/" + employee.getEmployeeId()));
                        });
                        return employeeIdButton;
                })
                                .setHeader("Employee ID")
                                .setAutoWidth(true);

                employeeGrid.addColumn(EmployeeDTO::getEmployeeName)
                                .setHeader("Employee Name")
                                .setAutoWidth(true);

                employeeGrid.addColumn(employee -> {
                        return employee.getDepartment() == null ? "" : employee.getDepartment().getDepartmentName();
                })
                                .setHeader("Department")
                                .setAutoWidth(true);

                employeeGrid.addColumn(employee -> {
                        return employee.getRole() == null ? "" : employee.getRole().getRoleName();
                })
                                .setHeader("Role")
                                .setAutoWidth(true);

                employeeGrid.addColumn(employee -> employee.getActive() ? "Yes" : "No")
                                .setHeader("Active")
                                .setAutoWidth(true);

                employeeGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

                employeeGrid.setSizeFull();

                // GRID ROW CLICK
                employeeGrid.addItemClickListener(event -> {

                        EmployeeDTO employee = event.getItem();

                        Notification.show(
                                        "Employee: " + employee.getEmployeeName()
                                                        + " | Department: " + employee.getDepartment()
                                                        + " | Role: " + employee.getRole(),
                                        5000,
                                        Notification.Position.TOP_CENTER);
                });

                // LOAD DEFAULT DATA
                loadEmployees();

                // employeeGrid.setItems(employeeList);

                add(headerLayout, filterLayout, employeeGrid, paginationLayout);

                expand(employeeGrid);

        }

        private void applyFilter() {

                Long employeeId = null;

                if (!employeeIdField.getValue().isEmpty()) {

                        employeeId = Long.valueOf(
                                        employeeIdField.getValue().trim());
                }

                currentFilter = new EmployeeDTO();

                currentFilter.setEmployeeId(employeeId);

                currentFilter.setEmployeeName(
                                employeeNameField.getValue());

                currentFilter.setDepartment(
                                departmentField.getValue());

                currentFilter.setRole(
                                roleField.getValue());

                currentFilter.setActive(
                                activeField.getValue() == null
                                                ? null
                                                : activeField.getValue().equals("Yes"));

                currentPage = 0;

                loadEmployees();
        }

        // CLEAR FILTER
        private void clearFilter() {

                employeeIdField.clear();
                employeeNameField.clear();
                departmentField.clear();
                roleField.clear();
                activeField.clear();

                currentFilter = new EmployeeDTO();

                currentPage = 0;

                loadEmployees();
        }

        // SAMPLE DATA
        private void loadEmployees() {

                Page<EmployeeDTO> employeePage = employeeService.getAllEmployees(
                                currentFilter,
                                currentPage,
                                pageSize);

                employeeGrid.setItems(
                                employeePage.getContent());

                pageInfo.setText("Page " + (currentPage + 1)
                                + " of " + employeePage.getTotalPages());
        }
}