package com.module.purchase.view;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.ViewPermission;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.ViewName;
import com.module.purchase.service.ViewPermissionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "view-permission", layout = MainLayout.class)
@PermitAll
public class ViewPermissionView extends VerticalLayout {

    private final ViewPermissionService viewPermissionService;
    
    private final Grid<ViewPermission> grid = new Grid<>(ViewPermission.class, false);

    private final ComboBox<ViewName> viewField = new ComboBox<>("View");

    private final ComboBox<EmployeeGroup> employeeGroupField = new ComboBox<>("Role Group");

    private int currentPage = 0;

    private int pageSize = 25;

    private final Span pageInfo =new Span();

    public ViewPermissionView( ViewPermissionService viewPermissionService, SecurityService securityService) {

        this.viewPermissionService = viewPermissionService;

        setSizeFull();

        setPadding(true);

        setSpacing(true);

        viewField.setItems(ViewName.values());

        viewField.setWidth("300px");

        employeeGroupField.setItems(EmployeeGroup.values());

        HorizontalLayout headerLayout = new HorizontalLayout();

        H2 title = new H2("View Permission Management");

        headerLayout.add(title);

        headerLayout.setWidthFull();

        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        headerLayout.setAlignItems( Alignment.CENTER);

        Button searchButton = new Button("Search");

        Button clearButton = new Button("Clear");

        Button addButton = new Button("Add Permission");

        searchButton.addClickListener(event -> {

            currentPage = 0;

            loadPermissions();
        });

        clearButton.addClickListener(event -> {

            viewField.clear();

            employeeGroupField.clear();

            currentPage = 0;

            loadPermissions();
        });

        addButton.addClickListener(event -> {

            try {

                if (viewField.isEmpty()
                        || employeeGroupField.isEmpty()) {

                    Notification.show(
                            "Please select all fields",
                            3000,
                            Notification.Position.TOP_CENTER);

                    return;
                }

                ViewPermission permission = new ViewPermission();

                permission.setViewName( viewField.getValue());

                permission.setEmployeeGroup( employeeGroupField.getValue());

                viewPermissionService.addPermission(permission, securityService.getLoggedInUser().getEmployee());

                Notification.show(
                        "Permission Added",
                        3000,
                        Notification.Position.TOP_CENTER);

                loadPermissions();

            } catch (Exception exception) {

                Notification.show(
                        exception.getMessage(),
                        5000,
                        Notification.Position.TOP_CENTER);
            }
        });

        HorizontalLayout filterLayout =
                new HorizontalLayout(
                        viewField,
                        employeeGroupField,
                        searchButton,
                        clearButton,
                        addButton);

        filterLayout.setWidthFull();

        filterLayout.setAlignItems(
                Alignment.END);

        grid.addColumn(ViewPermission::getId)
                .setHeader("ID")
                .setAutoWidth(true);

        grid.addColumn(permission ->
                permission.getViewName() == null
                        ? ""
                        : permission.getViewName().name())
                .setHeader("View")
                .setAutoWidth(true);

        grid.addColumn(permission ->
                permission.getEmployeeGroup() == null
                        ? ""
                        : permission.getEmployeeGroup().name())
                .setHeader("Role Group")
                .setAutoWidth(true);

        // DELETE BUTTON
        grid.addComponentColumn(permission -> {

            Button deleteButton =
                    new Button("Delete");

            deleteButton.addClickListener(event -> {

                ConfirmDialog dialog =
                        new ConfirmDialog();

                dialog.setHeader(
                        "Delete Permission");

                dialog.setText(
                        "Are you sure you want to delete this permission?");

                dialog.setCancelable(true);

                dialog.setConfirmText("Delete");

                dialog.setConfirmButtonTheme(
                        "error primary");

                dialog.addConfirmListener(confirmEvent -> {

                    try {

                        viewPermissionService.deleteById(permission.getId(),securityService.getLoggedInUser().getEmployee());

                        Notification.show(
                                "Permission Deleted",
                                3000,
                                Notification.Position.TOP_CENTER);

                        loadPermissions();

                    } catch (Exception exception) {

                        Notification.show(
                                exception.getMessage(),
                                5000,
                                Notification.Position.TOP_CENTER);
                    }
                });

                dialog.open();
            });

            return deleteButton;

        }).setHeader("Delete");

        grid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES);

        grid.setSizeFull();

        Button previousButton =new Button("Previous");

        Button nextButton = new Button("Next");

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

            loadPermissions();
        });

        previousButton.addClickListener(event -> {

            if (currentPage > 0) {

                currentPage--;

                loadPermissions();
            }
        });

        nextButton.addClickListener(event -> {

            currentPage++;

            loadPermissions();
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

        paginationLayout.setAlignItems(Alignment.CENTER);

        loadPermissions();

        add(
                headerLayout,
                filterLayout,
                grid,
                paginationLayout);

        expand(grid);
    }

    private void loadPermissions() {

        Page<ViewPermission> permissionPage =
                viewPermissionService
                        .getAllPermissions(
                                viewField.getValue(),
                                employeeGroupField.getValue(),
                                currentPage,
                                pageSize);

        grid.setItems(
                permissionPage.getContent());

        pageInfo.setText(
                "Page "
                        + (currentPage + 1)
                        + " of "
                        + permissionPage.getTotalPages());
    }

    public ComboBox<ViewName> getViewField() {
        return viewField;
    }
}