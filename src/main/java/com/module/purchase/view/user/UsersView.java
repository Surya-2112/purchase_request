package com.module.purchase.view.user;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Employee;
import com.module.purchase.entityDTO.UsersDTO;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.UsersService;
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
@Route(value = "user", layout = MainLayout.class)
@PermitAll
public class UsersView extends VerticalLayout {

    private final UsersService usersService;

    private final SecurityService securityService;


    private final Grid<UsersDTO> userGrid = new Grid<>(UsersDTO.class, false);

    private final TextField userIdField = new TextField("User ID");
    private final TextField userNameField = new TextField("User Name");
    private final TextField userEmailField = new TextField("User Email");

    private final ComboBox<Employee> employeeField = new ComboBox<>("Employee");
    private final ComboBox<String> activeField = new ComboBox<>("Active");

    private int currentPage = 0;
    private int pageSize = 25;

    private final Span pageInfo = new Span();

    private UsersDTO currentFilter = new UsersDTO();

    public UsersView(UsersService usersService, EmployeeService employeeService,SecurityService securityService) {

        this.usersService = usersService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // EMPLOYEE dropdown
        employeeField.setItems(employeeService.getEmployees());
        employeeField.setItemLabelGenerator(Employee::getEmployeeName);

        activeField.setItems("Yes", "No");

        // PAGINATION
        ComboBox<Integer> pageSizeField = new ComboBox<>();
        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(e -> {
            pageSize = e.getValue();
            currentPage = 0;
            loadUsers();
        });

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

        // HEADER
        H2 title = new H2("Users List");

        Button addButton = new Button("Add User", e -> {
            UsersForm form = new UsersForm(usersService, employeeService,securityService);
            form.open();
        });

        HorizontalLayout headerLayout = new HorizontalLayout(title, addButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // FILTER
        Button searchButton = new Button("Search", e -> applyFilter());
        Button clearButton = new Button("Clear", e -> clearFilter());

        HorizontalLayout filterLayout = new HorizontalLayout(
                userIdField,
                userNameField,
                userEmailField,
                employeeField,
                activeField,
                searchButton,
                clearButton);
        filterLayout.setAlignItems(Alignment.END);

        filterLayout.setWidthFull();

        // GRID
        userGrid.addComponentColumn(user -> {

            Button userIdButton = new Button(String.valueOf(user.getUserId()));

            userIdButton.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.navigate("user-details/" + user.getUserId()));
            });

            return userIdButton;
        })
                .setHeader("User ID")
                .setAutoWidth(true);

        userGrid.addColumn(UsersDTO::getUserName).setHeader("User Name").setAutoWidth(true);

        userGrid.addColumn(UsersDTO::getUserEmail).setHeader("User Email").setAutoWidth(true);

        userGrid.addColumn(user -> user.getEmployee() == null ? "" : user.getEmployee().getEmployeeName())
                .setHeader("Employee");

        userGrid.addColumn(user -> Boolean.TRUE.equals(user.getActive()) ? "Yes" : "No").setHeader("Active");

        userGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        userGrid.setSizeFull();

        userGrid.addItemClickListener(event -> {
            UsersDTO user = event.getItem();
            Notification.show("User: " + user.getUserName(), 3000,
                    Notification.Position.TOP_CENTER);
        });

        loadUsers();

        add(headerLayout, filterLayout, userGrid, paginationLayout);
        expand(userGrid);
    }

    private void loadUsers() {

        Page<UsersDTO> page = usersService.getAllUsers(
                currentFilter,
                currentPage,
                pageSize);

        userGrid.setItems(page.getContent());

        pageInfo.setText("Page " + (currentPage + 1)
                + " of " + page.getTotalPages());
    }

    private void applyFilter() {

        Long userId = null;

        if (!userIdField.getValue().isEmpty()) {
            userId = Long.valueOf(userIdField.getValue().trim());
        }

        currentFilter = new UsersDTO();
        currentFilter.setUserId(userId);
        currentFilter.setUserName(userNameField.getValue());
        currentFilter.setUserEmail(userEmailField.getValue());
        currentFilter.setEmployee(employeeField.getValue());

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
        employeeField.clear();
        activeField.clear();

        currentFilter = new UsersDTO();
        currentPage = 0;

        loadUsers();
    }
}