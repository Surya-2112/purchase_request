package com.module.purchase.view.role;

import java.util.stream.Collectors;

import com.module.purchase.entity.Role;
import com.module.purchase.service.RoleService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "role-details", layout = MainLayout.class)
@PermitAll
public class RoleDetailsView extends VerticalLayout
                implements HasUrlParameter<Long> {

        private final RoleService roleService;

        public RoleDetailsView(
                        RoleService roleService) {

                this.roleService = roleService;

                setSizeFull();

                setPadding(true);

                setSpacing(true);
        }

        @Override
        public void setParameter(
                        BeforeEvent event,
                        Long roleId) {

                removeAll();

                Role role = roleService
                                .getRoleById(roleId)
                                .orElse(null);

                if (role == null) {

                        add(new Span("Role Not Found"));

                        return;
                }

                H2 title = new H2("Role Details");

                FormLayout formLayout = new FormLayout();

                // ROLE ID
                formLayout.addFormItem(
                                new Span(
                                                String.valueOf(
                                                                role.getRoleId())),
                                "Role ID");

                // ROLE NAME
                formLayout.addFormItem(
                                new Span(
                                                role.getRoleName() == null
                                                                ? ""
                                                                : role.getRoleName()),
                                "Role Name");

                // EMPLOYEE GROUPS
                String employeeGroups = role.getEmployeeGroups() == null
                                ? ""
                                : role.getEmployeeGroups()
                                                .stream()
                                                .map(Enum::name)
                                                .collect(Collectors.joining(", "));

                formLayout.addFormItem(
                                new Span(employeeGroups),
                                "Employee Groups");

                // UPDATE BUTTON
                Button updateButton = new Button("Update");

                updateButton.addClickListener(clickEvent -> {

                        getUI().ifPresent(ui -> ui.navigate(
                                        "role-edit/"
                                                        + role.getRoleId()));
                });

                // DELETE BUTTON
                Button deleteButton = new Button("Delete");

                deleteButton.addClickListener(clickEvent -> {

                        ConfirmDialog dialog = new ConfirmDialog();

                        dialog.setHeader("Delete Role");

                        dialog.setText("Are you sure you want to delete this role?");

                        dialog.setCancelable(true);

                        dialog.setConfirmText("Delete");

                        dialog.setConfirmButtonTheme("error primary");

                        dialog.addConfirmListener(confirmEvent -> {

                                try { // CALL SERVICE
                                        roleService.deleteRoleById(role.getRoleId());

                                        Notification.show("Role Deleted Successfully",
                                                        3000,
                                                        Notification.Position.TOP_CENTER);

                                        getUI().ifPresent(ui -> ui.navigate("role"));

                                } catch (Exception exception) {

                                        Notification.show(
                                                        exception.getMessage(),
                                                        5000,
                                                        Notification.Position.TOP_CENTER);
                                }
                        });

                        dialog.open();
                });
                HorizontalLayout buttonLayout = new HorizontalLayout(updateButton,deleteButton);

                add( title,formLayout, buttonLayout);
        }
}