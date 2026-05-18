package com.module.purchase.view;

import org.springframework.security.core.context.SecurityContextHolder;
import com.vaadin.flow.component.Component;

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
import com.vaadin.flow.component.html.H2;
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

        setPrimarySection(
                Section.DRAWER);

        // ================= CURRENT USER =================

        Users loggedInUser =
                securityService.getLoggedInUser();

        String employeeName =
                loggedInUser.getEmployee() == null
                        ? loggedInUser.getUserName()
                        : loggedInUser.getEmployee()
                                .getEmployeeName();

        // ================= HEADER =================

        H2 title =
                new H2("Purchase Management");

        title.getStyle()
                .set("margin", "0");

        HorizontalLayout header =
                new HorizontalLayout(title);

        header.setWidthFull();

        header.setPadding(true);

        header.setAlignItems(
                FlexComponent.Alignment.CENTER);

        addToNavbar(header);

        // ================= PROFILE SECTION =================

        Avatar avatar =
                new Avatar(employeeName);

        avatar.setWidth("45px");

        avatar.setHeight("45px");

        H3 userName =
                new H3(employeeName);

        userName.getStyle()
                .set("margin", "0")
                .set("font-size", "18px");

        VerticalLayout profileText =
                new VerticalLayout(userName);

        profileText.setPadding(false);

        profileText.setSpacing(false);

        HorizontalLayout profileSection =
                new HorizontalLayout(
                        avatar,
                        profileText);

        profileSection.setAlignItems(
                FlexComponent.Alignment.CENTER);

        profileSection.setWidthFull();

        profileSection.setPadding(true);

        profileSection.getStyle()
                .set("cursor", "pointer")
                .set("border-bottom",
                        "1px solid #e5e5e5");

        // ================= PROFILE CLICK =================

        profileSection.addClickListener(event -> {

            getUI().ifPresent(ui ->

                    ui.navigate(
                            "user-details/"
                                    + loggedInUser.getUserId()));
        });

        // ================= MENU LINKS =================

        VerticalLayout menuLinks =
                new VerticalLayout();

        menuLinks.setPadding(false);

        menuLinks.setSpacing(true);

        menuLinks.setWidthFull();

        menuLinks.add(

                createLink(
                        "Dashboard",
                        DashboardView.class),

                createLink(
                        "Item",
                        ItemView.class),

                createLink(
                        "Purchase Request",
                        PurchaseRequestView.class),

                createLink(
                        "Purchase Order",
                        PurchaseOrderView.class),

                createLink(
                        "Vendor",
                        VendorView.class),

                createLink(
                        "Vendor Category",
                        VendorCategoryView.class),

                createLink(
                        "Employee",
                        EmployeeView.class),

                createLink(
                        "Department",
                        DepartmentView.class),

                createLink(
                        "Department Budget",
                        DepartmentBudgetView.class),

                createLink(
                        "Role",
                        RoleView.class),

                createLink(
                        "User",
                        UsersView.class),

                createLink(
                        "Assigning Config",
                        AssigningConfigView.class),

                createLink(
                        "Audit Logs",
                        AuditLogsView.class));

        // ================= SCROLLER =================

        Scroller scroller =
                new Scroller(menuLinks);

        scroller.setSizeFull();

        // ================= LOGOUT BUTTON =================

        Button logoutButton =
                new Button(
                        "Logout",
                        VaadinIcon.SIGN_OUT.create());

        logoutButton.setWidthFull();

        logoutButton.getStyle()
                .set("margin-top", "10px");

        logoutButton.addClickListener(event -> {

            SecurityContextHolder.clearContext();

            getUI().ifPresent(ui -> {

                ui.getSession().close();

                ui.getPage().setLocation("/login");
            });
        });

        // ================= DRAWER LAYOUT =================

        VerticalLayout drawerLayout =
                new VerticalLayout(
                        profileSection,
                        scroller,
                        logoutButton);

        drawerLayout.setSizeFull();

        drawerLayout.setPadding(false);

        drawerLayout.setSpacing(false);

        drawerLayout.expand(scroller);

        drawerLayout.getStyle()
                .set("overflow", "hidden");

        addToDrawer(drawerLayout);

        // ================= DRAWER WIDTH =================

        getStyle().set(
                "--vaadin-app-layout-drawer-width",
                "260px");
    }

    // ================= CREATE MENU LINK =================

   private RouterLink createLink(
        String text,
        Class<? extends Component> navigationTarget) {

        RouterLink link = new RouterLink(text, navigationTarget);

      //  link.setWidthFull();

        link.getStyle()
                .set("padding", "10px 15px")
                .set("border-radius", "8px")
                .set("text-decoration", "none")
                .set("font-size", "15px");

        return link;
    }
}