package com.module.purchase.view;

import org.springframework.security.core.context.SecurityContextHolder;

import com.module.purchase.entity.Users;
import com.module.purchase.service.SecurityService;
import com.module.purchase.view.assigningConfig.AssigningConfigView;
import com.module.purchase.view.auditLogs.AuditLogsView;
import com.module.purchase.view.department.DepartmentView;
import com.module.purchase.view.departmentBudget.DepartmentBudgetView;
import com.module.purchase.view.employee.EmployeeView;
import com.module.purchase.view.item.ItemView;
import com.module.purchase.view.purchaseOrder.PurchaseOrderView;
import com.module.purchase.view.purchaseRequest.PurchaseRequestView;
import com.module.purchase.view.role.RoleView;
import com.module.purchase.view.user.UsersView;
import com.module.purchase.view.vendor.VendorView;
import com.module.purchase.view.vendorCategory.VendorCategoryView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

import jakarta.annotation.security.PermitAll;

@PermitAll
public class MainLayout extends AppLayout {

        private final SecurityService securityService;

        public MainLayout(SecurityService securityService) {

                this.securityService = securityService;

                // ================= CURRENT USER =================

                Users loggedInUser = securityService.getLoggedInUser();

                String employeeName = loggedInUser.getEmployee() == null
                                ? loggedInUser.getUserName()
                                : loggedInUser.getEmployee()
                                                .getEmployeeName();

                // ================= PROFILE SECTION =================

                Avatar avatar = new Avatar(employeeName);

                H3 userName = new H3(employeeName);

                VerticalLayout profileText = new VerticalLayout(userName);

                profileText.setPadding(false);
                profileText.setSpacing(false);

                HorizontalLayout profileSection = new HorizontalLayout(
                                avatar,
                                profileText);

                profileSection.setAlignItems(
                                FlexComponent.Alignment.CENTER);

                profileSection.setWidthFull();

                profileSection.getStyle()
                                .set("cursor", "pointer");

                // Navigate to current user details
                profileSection.addClickListener(event -> {

                        getUI().ifPresent(ui ->

                        ui.navigate(
                                        "user-details/"
                                                        + loggedInUser.getUserId()));
                });

                // ================= MENU LINKS =================

                VerticalLayout menuLinks = new VerticalLayout(

                                new RouterLink(
                                                "Dashboard",
                                                DashboardView.class),

                                new RouterLink(
                                                "Item",
                                                ItemView.class),

                                new RouterLink("PurchaseRequest",PurchaseRequestView.class),

                                new RouterLink(
                                                "PurchaseOrder",
                                                PurchaseOrderView.class),

                                new RouterLink(
                                                "Vendor",
                                                VendorView.class),

                                new RouterLink(
                                                "VendorCategory",
                                                VendorCategoryView.class),

                                new RouterLink(
                                                "Employee",
                                                EmployeeView.class),

                                new RouterLink(
                                                "Department",
                                                DepartmentView.class),

                                new RouterLink(
                                                "DepartmentBudget",
                                                DepartmentBudgetView.class),

                                new RouterLink(
                                                "Role",
                                                RoleView.class),

                                new RouterLink(
                                                "User",
                                                UsersView.class),

                                new RouterLink(
                                                "AssigningConfig",
                                                AssigningConfigView.class),

                                new RouterLink(
                                                "AuditLogs",
                                                AuditLogsView.class));

                menuLinks.setPadding(false);
                menuLinks.setSpacing(true);

                // ================= MENU SCROLLER =================

                Scroller scroller = new Scroller(menuLinks);

                scroller.setSizeFull();

                // ================= LOGOUT BUTTON =================

                Button logoutButton = new Button(
                                "Logout",
                                VaadinIcon.SIGN_OUT.create());

                logoutButton.setWidthFull();

                logoutButton.addClickListener(event -> {
                        SecurityContextHolder.clearContext();
                        getUI().ifPresent(ui ->{
                        ui.getSession().close();
                        ui.getPage().setLocation("/login");
                 });
                });

                // ================= DRAWER LAYOUT =================

                VerticalLayout drawerLayout = new VerticalLayout(
                                profileSection,
                                scroller,
                                logoutButton);

                drawerLayout.setSizeFull();

                drawerLayout.expand(scroller);

                addToDrawer(drawerLayout);

                // ================= DRAWER WIDTH =================

                getStyle().set(
                                "--vaadin-app-layout-drawer-width",
                                "200px");
        }
}