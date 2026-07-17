package com.module.purchase.view.employee;

import java.util.List;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Role;
import com.module.purchase.entity.Users;
import com.module.purchase.entityDTO.EmployeeDTO;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.RoleService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
        private final SecurityService securityService;
        private final Grid<EmployeeDTO> employeeGrid = new Grid<>(EmployeeDTO.class, false);

        private final TextField employeeIdField = new TextField("Employee ID");
        private final TextField employeeNameField = new TextField("Employee Name");

        private final ComboBox<Department> departmentField = new ComboBox<>("Department");
        private final ComboBox<Role> roleField = new ComboBox<>("Role");
        private final ComboBox<String> activeField = new ComboBox<>("Active");
        private int currentPage = 0;
        private int pageSize = 25;
        private int totalPage = 1;

        private final Span pageInfo = new Span();
        private final HorizontalLayout paginationLayout = new HorizontalLayout();

        private EmployeeDTO currentFilter = new EmployeeDTO();

        public EmployeeView(EmployeeService employeeService, RoleService roleService,
                        DepartmentService departmentService, SecurityService securityService) {
                this.employeeService = employeeService;
                this.securityService = securityService;

                setSizeFull();
                setPadding(true);
                setSpacing(true);

                employeeIdField.setWidth("100px");
                employeeIdField.setPattern("[0-9]{0,20}");
                employeeIdField.setErrorMessage("Enter a valid number");

                departmentField.setItems(departmentService.getDepartments());

                roleField.setItems(roleService.getRoles());

                departmentField.setItemLabelGenerator(Department::getDepartmentName);
                roleField.setItemLabelGenerator(Role::getRoleName);

                activeField.setItems("Yes", "No");
                activeField.setWidth("100px");

                Button previousButton = new Button("Previous");
                previousButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                Button nextButton = new Button("Next");
                nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

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
                        if (currentPage < totalPage - 1) {
                                currentPage++;
                                loadEmployees();
                        }
                });

                paginationLayout.add(previousButton, pageInfo, nextButton, new Span("Page Size"), pageSizeField);
                paginationLayout.setWidthFull();
                paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
                paginationLayout.setAlignItems(Alignment.CENTER);

                HorizontalLayout headerLayout = new HorizontalLayout();
                H2 title = new H2("Employees List");

                Button addButton = new Button("Add Employee");
                addButton.addClickListener(event -> {
                        getUI().ifPresent(ui -> ui.navigate("employee-form"));
                });

                boolean hasFormAccess = securityService.canAccessView("employee-form");
                addButton.setVisible(hasFormAccess);
                addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

                headerLayout.add(title, addButton);
                headerLayout.setWidthFull();
                headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
                headerLayout.setAlignItems(Alignment.CENTER);

                HorizontalLayout filterLayout = new HorizontalLayout();
                Button searchButton = new Button("Search", event -> applyFilter());
                searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                Button clearButton = new Button("Clear", event -> clearFilter());
                clearButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

                filterLayout.setAlignItems(Alignment.END);
                filterLayout.add(employeeIdField, employeeNameField, departmentField, roleField, activeField,
                                searchButton, clearButton);
                filterLayout.setWidthFull();

                if (!hasFormAccess) {
                        filterLayout.setVisible(false);
                }

                employeeGrid.addColumn(EmployeeDTO::getEmployeeId).setHeader("Employee ID").setAutoWidth(true);
                employeeGrid.addColumn(EmployeeDTO::getEmployeeName).setHeader("Employee Name").setAutoWidth(true);
                employeeGrid.addColumn(employee -> employee.getDepartment() == null ? ""
                                : employee.getDepartment().getDepartmentName()).setHeader("Department")
                                .setAutoWidth(true);
                employeeGrid.addColumn(employee -> employee.getRole() == null ? "" : employee.getRole().getRoleName())
                                .setHeader("Role").setAutoWidth(true);
                employeeGrid.addComponentColumn(employee -> {
                        Span badge = new Span(Boolean.TRUE.equals(employee.getActive()) ? "Yes" : "No");
                        badge.getStyle()
                                        .set("padding", "2px 8px")
                                        .set("border-radius", "4px")
                                        .set("font-weight", "bold")
                                        .set("font-size", "12px");
                        if (Boolean.TRUE.equals(employee.getActive())) {
                                badge.getStyle().set("background-color", "#dcfce7").set("color", "#15803d");
                        } else {
                                badge.getStyle().set("background-color", "#fee2e2").set("color", "#b91c1c");
                        }
                        return badge;
                }).setHeader("Active").setAutoWidth(true);

                employeeGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
                employeeGrid.setSizeFull();
                employeeGrid.getStyle().set("border-radius", "12px").set("overflow", "hidden");

                employeeGrid.addItemDoubleClickListener(event -> {
                        EmployeeDTO employee = event.getItem();
                        if (securityService.getLoggedInUser().getEmployee().getEmployeeId()
                                        .equals(employee.getEmployeeId())
                                        || securityService.getLoggedInUser().getEmployee().getRole().getEmployeeGroups()
                                                        .contains(EmployeeGroup.MANAGER)) {
                                getUI().ifPresent(ui -> ui.navigate("employee-details/" + employee.getEmployeeId()));
                        } else {
                                Notification.show("Access Denied", 3000, Notification.Position.MIDDLE);
                        }
                });

                loadEmployees();

                add(headerLayout, filterLayout, employeeGrid, paginationLayout);
                expand(employeeGrid);
        }

        private void applyFilter() {
                Long employeeId = null;
                if (!employeeIdField.getValue().isEmpty()) {
                        employeeId = Long.valueOf(employeeIdField.getValue().trim());
                }

                currentFilter = new EmployeeDTO();
                currentFilter.setEmployeeId(employeeId);
                currentFilter.setEmployeeName(employeeNameField.getValue());
                currentFilter.setDepartment(departmentField.getValue());
                currentFilter.setRole(roleField.getValue());
                currentFilter.setActive(activeField.getValue() == null ? null : activeField.getValue().equals("Yes"));

                currentPage = 0;
                loadEmployees();
        }

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

        private EmployeeDTO convertToDto(Employee employee) {
                if (employee == null) {
                        return null;
                }
                EmployeeDTO dto = new EmployeeDTO();
                dto.setEmployeeId(employee.getEmployeeId());
                dto.setEmployeeName(employee.getEmployeeName());
                dto.setDepartment(employee.getDepartment());
                dto.setRole(employee.getRole());
                dto.setActive(employee.getActive());
                return dto;
        }

        private void loadEmployees() {
                boolean hasFormAccess = securityService.canAccessView("management-group");

                if (hasFormAccess) {
                        Page<EmployeeDTO> employeePage = employeeService.getAllEmployees(currentFilter, currentPage,
                                        pageSize);
                        employeeGrid.setItems(employeePage.getContent());
                        pageInfo.setText("Page " + (currentPage + 1) + " of " + employeePage.getTotalPages());
                        paginationLayout.setVisible(true);
                        totalPage = employeePage.getTotalPages();
                } else {
                        Users user = securityService.getLoggedInUser();
                        if (user != null && user.getEmployee() != null) {
                                Employee selfDto = employeeService.getEmployeeById(user.getEmployee().getEmployeeId())
                                                .orElse(null);
                                if (selfDto != null) {
                                        employeeGrid.setItems(List.of(convertToDto(selfDto)));
                                } else {
                                        employeeGrid.setItems(List.of());
                                }
                        }
                        pageInfo.setText("Page 1 of 1");
                        totalPage = 1;
                        paginationLayout.setVisible(false);
                }
        }
}