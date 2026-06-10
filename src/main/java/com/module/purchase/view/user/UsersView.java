package com.module.purchase.view.user;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Vendor;
import com.module.purchase.entityDTO.UsersDTO;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.UsersService;
import com.module.purchase.service.VendorService;
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

@Route(value = "user", layout = MainLayout.class)
@PermitAll
public class UsersView extends VerticalLayout {

    private final UsersService usersService;

    private final Grid<UsersDTO> userGrid = new Grid<>(UsersDTO.class, false);

    private final TextField userIdField = new TextField("User ID");
    private final TextField userNameField = new TextField("User Name");
    private final TextField userEmailField = new TextField("User Email");

    private final ComboBox<String> userTypeField = new ComboBox<>("User Type");

    private final ComboBox<Employee> employeeField = new ComboBox<>("Employee");
    private final ComboBox<Vendor> vendorField = new ComboBox<>("Vendor");

    private final ComboBox<String> activeField = new ComboBox<>("Active");

    private int currentPage = 0;
    private int pageSize = 25;

    private final Span pageInfo = new Span();

    private UsersDTO currentFilter = new UsersDTO();

    public UsersView(
            UsersService usersService,
            EmployeeService employeeService,
            SecurityService securityService,
            VendorService vendorService) {

        this.usersService = usersService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        userIdField.setWidth("100px");

        // Employee
        employeeField.setItems(employeeService.getEmployees());
        employeeField.setItemLabelGenerator(Employee::getEmployeeName);

        // Vendor
        vendorField.setItems(vendorService.getVendors());
        vendorField.setItemLabelGenerator(Vendor::getVendorName);

        // User Type
        userTypeField.setItems("Employee", "Vendor");

        employeeField.setVisible(false);
        vendorField.setVisible(false);

        userTypeField.addValueChangeListener(event -> {

            employeeField.clear();
            vendorField.clear();

            if ("Employee".equals(event.getValue())) {

                employeeField.setVisible(true);
                vendorField.setVisible(false);

            } else if ("Vendor".equals(event.getValue())) {

                employeeField.setVisible(false);
                vendorField.setVisible(true);

            } else {

                employeeField.setVisible(false);
                vendorField.setVisible(false);
            }
        });

        // Active
        activeField.setItems("Yes", "No");
        activeField.setWidth("80px");
        // Page Size
        ComboBox<Integer> pageSizeField = new ComboBox<>();
        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(e -> {
            pageSize = e.getValue();
            currentPage = 0;
            loadUsers();
        });

        // Pagination
        Button previousButton = new Button("Previous", e -> {
            if (currentPage > 0) {
                currentPage--;
                loadUsers();
            }
        });

        Button nextButton = new Button("Next", e -> {
            currentPage++;
            loadUsers();
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

        // Header
        H2 title = new H2("Users List");

        Button addButton = new Button("Add User", e -> {

            UsersForm form = new UsersForm(
                    usersService,
                    employeeService,
                    vendorService,
                    securityService);

            form.open();
        });

        addButton.setVisible(
                securityService.canAccessView("user-form"));

        HorizontalLayout headerLayout =
                new HorizontalLayout(title, addButton);

        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(
                JustifyContentMode.BETWEEN);

        // Search Buttons
        Button searchButton =
                new Button("Search", e -> applyFilter());

        Button clearButton =
                new Button("Clear", e -> clearFilter());

        HorizontalLayout filterLayout =
                new HorizontalLayout(
                        userIdField,
                        userNameField,
                        userEmailField,
                        userTypeField,
                        employeeField,
                        vendorField,
                        activeField,
                        searchButton,
                        clearButton);

        filterLayout.setAlignItems(Alignment.END);
        filterLayout.setWidthFull();

        // Grid Columns

        userGrid.addColumn(UsersDTO::getUserId)
                .setHeader("User ID")
                .setAutoWidth(true);

        userGrid.addColumn(UsersDTO::getUserName)
                .setHeader("User Name")
                .setAutoWidth(true);

        userGrid.addColumn(UsersDTO::getUserEmail)
                .setHeader("User Email")
                .setAutoWidth(true);

        userGrid.addColumn(user -> {

            if (user.getEmployee() != null) {
                return "Employee";
            }

            if (user.getVendor() != null) {
                return "Vendor";
            }

            return "";

        }).setHeader("User Type")
          .setAutoWidth(true);

        userGrid.addColumn(user -> {

            if (user.getEmployee() != null) {
                return user.getEmployee().getEmployeeName();
            }

            if (user.getVendor() != null) {
                return user.getVendor().getVendorName();
            }

            return "";

        }).setHeader("Linked To")
          .setAutoWidth(true);

        userGrid.addColumn(user ->
                Boolean.TRUE.equals(user.getActive())
                        ? "Yes"
                        : "No")
                .setHeader("Active")
                .setAutoWidth(true);

        userGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        userGrid.setSizeFull();

        userGrid.addItemDoubleClickListener(event -> {

            UsersDTO user = event.getItem();

            getUI().ifPresent(ui ->
                    ui.navigate(
                            "user-details/" + user.getUserId()));
        });

        loadUsers();

        add(
                headerLayout,
                filterLayout,
                userGrid,
                paginationLayout);

        expand(userGrid);
    }

    private void loadUsers() {

        Page<UsersDTO> page =
                usersService.getAllUsers(
                        currentFilter,
                        currentPage,
                        pageSize);

        userGrid.setItems(page.getContent());

        pageInfo.setText(
                "Page "
                        + (currentPage + 1)
                        + " of "
                        + Math.max(page.getTotalPages(), 1));
    }

    private void applyFilter() {

        Long userId = null;

        if (!userIdField.getValue().isEmpty()) {

            try {
                userId = Long.valueOf(
                        userIdField.getValue().trim());
            } catch (Exception e) {
                return;
            }
        }

        currentFilter = new UsersDTO();

        currentFilter.setUserId(userId);
        currentFilter.setUserName(userNameField.getValue());
        currentFilter.setUserEmail(userEmailField.getValue());

        if ("Employee".equals(userTypeField.getValue())) {
            currentFilter.setEmployee(employeeField.getValue());
        }

        if ("Vendor".equals(userTypeField.getValue())) {
            currentFilter.setVendor(vendorField.getValue());
        }

        currentFilter.setActive(
                activeField.getValue() == null
                        ? null
                        : activeField.getValue().equals("Yes"));

        currentPage = 0;

        loadUsers();
    }

    private void clearFilter() {

        userIdField.clear();
        userNameField.clear();
        userEmailField.clear();

        userTypeField.clear();
        employeeField.clear();
        vendorField.clear();

        employeeField.setVisible(false);
        vendorField.setVisible(false);

        activeField.clear();

        currentFilter = new UsersDTO();

        currentPage = 0;

        loadUsers();
    }
}