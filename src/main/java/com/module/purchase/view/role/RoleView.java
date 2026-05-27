package com.module.purchase.view.role;

import org.springframework.data.domain.Page;

import java.util.List;
import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Role;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.service.RoleService;
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

@Route(value = "role", layout = MainLayout.class)
@PermitAll
public class RoleView extends VerticalLayout {

    private final RoleService roleService;

  //  private final SecurityService securityService;


    private final Grid<Role> roleGrid = new Grid<>(Role.class, false);

    // FILTERS
    private final TextField roleIdField = new TextField("Role ID");

    private final TextField roleNameField = new TextField("Role Name");

    private final ComboBox<EmployeeGroup> employeeGroupField = new ComboBox<>("Role Group");

    // PAGINATION
    private int currentPage = 0;

    private int pageSize = 25;

    private final Span pageInfo = new Span();

    private Role currentFilter = new Role();

    public RoleView(RoleService roleService,SecurityService securityService) {

        this.roleService = roleService;
  //      this.securityService = securityService;

        setSizeFull();

        setPadding(true);

        setSpacing(true);

        // EMPLOYEE GROUPS
        employeeGroupField.setItems(EmployeeGroup.values());

        // PAGINATION
        Button previousButton = new Button("Previous");

        Button nextButton =new Button("Next");

        ComboBox<Integer> pageSizeField =new ComboBox<>();

        pageSizeField.setItems(10, 25, 50, 100);

        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(event -> {

            pageSize =event.getValue();

            currentPage = 0;

            loadRoles();
        });

        previousButton.addClickListener(event -> {

            if (currentPage > 0) {

                currentPage--;

                loadRoles();
            }
        });

        nextButton.addClickListener(event -> {

            currentPage++;

            loadRoles();
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

        // HEADER
        HorizontalLayout headerLayout = new HorizontalLayout();

        H2 title = new H2("Role List");

        Button addButton = new Button("Add Role");

        addButton.addClickListener(event -> {

            RoleForm form = new RoleForm(roleService,securityService);

            form.open();
        });

        addButton.setVisible(securityService.canAccessView("role-form"));

        headerLayout.add( title, addButton);

        headerLayout.setWidthFull();

        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        headerLayout.setAlignItems(Alignment.CENTER);

        // FILTER
        HorizontalLayout filterLayout =new HorizontalLayout();

        Button searchButton =
                new Button("Search",event -> applyFilter());

        Button clearButton =
                new Button( "Clear", event -> clearFilter());

        filterLayout.setAlignItems( Alignment.END);

        filterLayout.add(
                roleIdField,
                roleNameField,
                employeeGroupField,
                searchButton,
                clearButton);

        filterLayout.setWidthFull();

        // GRID COLUMNS

        // ROLE ID
        roleGrid.addColumn(Role::getRoleId)
        .setHeader("Role ID")
        .setAutoWidth(true);

        // ROLE NAME
        roleGrid.addColumn(role -> {

            return role.getRoleName() == null
                    ? "" : role.getRoleName();

        })
        .setHeader("Role Name")
        .setAutoWidth(true);

        // EMPLOYEE GROUPS
        roleGrid.addColumn(role -> {

            if (role.getEmployeeGroups() == null || role.getEmployeeGroups().isEmpty()) {
                return "";
            }

            return String.join( ", ", role.getEmployeeGroups().stream().map(EmployeeGroup::name)
                            .toList());

        })
        .setHeader("Role Groups")
        .setAutoWidth(true);

        roleGrid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES);

        roleGrid.setSizeFull();

        // GRID ROW CLICK
        roleGrid.addItemClickListener(event -> {

            Role role = event.getItem();

            getUI().ifPresent(ui ->
                        ui.navigate(
                                "role-details/"
                                        + role.getRoleId()));

        });

        // LOAD DATA
        loadRoles();

        add(    headerLayout,
                filterLayout,
                roleGrid,
                paginationLayout);

        expand(roleGrid);
    }

    private void loadRoles() {

        Page<Role> rolePage =
                roleService.getAllRoles(
                        currentFilter,
                        currentPage,
                        pageSize);

        roleGrid.setItems(
                rolePage.getContent());

        pageInfo.setText(
                "Page "
                        + (currentPage + 1)
                        + " of "
                        + rolePage.getTotalPages());
    }

    private void applyFilter() {

        Long roleId = null;

        if (!roleIdField
                .getValue()
                .isEmpty()) {

            roleId = Long.valueOf(
                    roleIdField
                            .getValue()
                            .trim());
        }

        currentFilter = new Role();

        currentFilter.setRoleId(roleId);

        currentFilter.setRoleName(roleNameField.getValue());

        if (employeeGroupField.getValue() != null) {

            currentFilter.setEmployeeGroups( List.of(employeeGroupField.getValue()));
        }

        currentPage = 0;

        loadRoles();
    }

    private void clearFilter() {

        roleIdField.clear();

        roleNameField.clear();

        employeeGroupField.clear();

        currentFilter =
                new Role();

        currentPage = 0;

        loadRoles();
    }
}