package com.module.purchase.view.role;

import java.util.stream.Collectors;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Role;
import com.module.purchase.enums.ViewName;
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
public class RoleDetailsView extends VerticalLayout implements HasUrlParameter<String> {

        private final RoleService roleService;

        private final SecurityService securityService;

        public RoleDetailsView( RoleService roleService,SecurityService securityService) {

                this.roleService = roleService;
                this.securityService=securityService;

                setSizeFull();

                setPadding(true);

                setSpacing(true);
        }

        @Override
        public void setParameter(BeforeEvent event, String roleId) {

                removeAll();
                try{
                Role role = roleService.getRoleById(Long.parseLong(roleId)).orElse(null);
                if (role == null) {
                        add(new Span("Role Not Found"));
                        return;
                }

                H2 title = new H2("Role Details");

                FormLayout formLayout = new FormLayout();

                formLayout.addFormItem(new Span(String.valueOf(role.getRoleId())), "Role ID");
                formLayout.addFormItem( new Span( role.getRoleName() == null ? "" : role.getRoleName()), "Role Name");
                String employeeGroups = role.getEmployeeGroups() == null? "" : role.getEmployeeGroups().stream().map(Enum::name).collect(Collectors.joining(", "));
                formLayout.addFormItem( new Span(employeeGroups),"Role Groups");

                Button updateButton = new Button("Update");

                updateButton.addClickListener(clickEvent -> {
                        getUI().ifPresent(ui -> ui.navigate("role-edit/"+ role.getRoleId()));
                });

                Button deleteButton = new Button("Delete");

                deleteButton.addClickListener(clickEvent -> {

                        ConfirmDialog dialog = new ConfirmDialog();

                        dialog.setHeader("Delete Role");

                        dialog.setText("Are you sure you want to delete this role?");

                        dialog.setCancelable(true);

                        dialog.setConfirmText("Delete");

                        dialog.setConfirmButtonTheme("error primary");

                        dialog.addConfirmListener(confirmEvent -> {

                                try { 
                                        roleService.deleteRoleById(role.getRoleId(),securityService.getLoggedInUser().getEmployee());
                                        Notification.show("Role Deleted Successfully",3000,  Notification.Position.TOP_CENTER);
                                        getUI().ifPresent(ui -> ui.navigate("role"));
                                } catch (Exception exception) {
                                        Notification.show( exception.getMessage(), 5000,Notification.Position.TOP_CENTER);
                                }
                        });

                        dialog.open();
                });
                
                updateButton.setVisible(securityService.canAccessView("role-edit"));
                deleteButton.setVisible(securityService.canAccessView("role-form"));
                
                HorizontalLayout buttonLayout = new HorizontalLayout(updateButton,deleteButton);

                add( title,formLayout, buttonLayout);
        }catch (NumberFormatException e) {
            event.forwardTo(ViewName.ROLE.getRoute());
            event.getUI().access(() -> {
            Notification.show("url is not valid ," + e.getMessage(), 3000,Notification.Position.TOP_CENTER);});
            return;
        }catch (Exception ex) {
            event.forwardTo(ViewName.ROLE.getRoute());
            event.getUI().access(() -> {Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);});
            return;
        }
        }
}