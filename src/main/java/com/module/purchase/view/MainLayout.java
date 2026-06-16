package com.module.purchase.view;

import org.springframework.security.core.context.SecurityContextHolder;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Users;
import com.module.purchase.enums.ViewName;
import com.module.purchase.view.assigningConfig.AssigningConfigView;
import com.module.purchase.view.auditLogs.AuditLogView;
import com.module.purchase.view.category.CategoryView;
import com.module.purchase.view.department.DepartmentView;
import com.module.purchase.view.departmentBudget.DepartmentBudgetView;
import com.module.purchase.view.employee.EmployeeView;
import com.module.purchase.view.item.ItemView;
import com.module.purchase.view.itemvariant.ItemVariantView;
import com.module.purchase.view.purchaseOrder.PurchaseOrderView;
import com.module.purchase.view.purchaseRequest.PurchaseRequestView;
import com.module.purchase.view.quotation.QuotationComparisonView;
import com.module.purchase.view.quotation.QuotationView;
import com.module.purchase.view.repeatedPeriod.RepeatedPeriodView;
import com.module.purchase.view.requestForQuotation.RequestForQuotationView;
import com.module.purchase.view.role.RoleView;
import com.module.purchase.view.unit.UnitView;
import com.module.purchase.view.user.UsersView;
import com.module.purchase.view.vendor.VendorView;
import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLink;

import jakarta.annotation.security.PermitAll;

@PermitAll
public class MainLayout extends AppLayout implements AfterNavigationObserver {

        private final VerticalLayout menuLinks = new VerticalLayout();

        public MainLayout(SecurityService securityService) {

                setPrimarySection(Section.DRAWER);

                Users loggedInUser = securityService.getLoggedInUser();

                String employeeName = loggedInUser.getEmployee() == null
                                ? loggedInUser.getUserName()
                                : loggedInUser.getEmployee()
                                                .getEmployeeName();

                H2 title = new H2("Purchase Management");

                title.getStyle().set("margin", "0");

                HorizontalLayout header = new HorizontalLayout(title);

                header.setWidthFull();
                header.setPadding(true);
                header.setAlignItems(FlexComponent.Alignment.CENTER);

                addToNavbar(header);

                Avatar avatar = new Avatar(employeeName);
                avatar.setWidth("45px");
                avatar.setHeight("45px");

                H3 userName = new H3(employeeName);
                userName.getStyle()
                                .set("margin", "0")
                                .set("font-size", "18px");

                VerticalLayout profileText = new VerticalLayout(userName);
                profileText.setPadding(false);
                profileText.setSpacing(false);

                HorizontalLayout profileSection = new HorizontalLayout(
                                avatar,
                                profileText);

                profileSection.setAlignItems(FlexComponent.Alignment.CENTER);
                profileSection.setWidthFull();
                profileSection.setPadding(true);
                profileSection.getStyle().set("cursor", "pointer")
                                .set("border-bottom", "1px solid #e5e5e5");

                profileSection.addClickListener(event -> {
                        getUI().ifPresent(ui ->
                        ui.navigate(ViewName.USER_DETAILS.getRoute()+"/"+ loggedInUser.getUserId()));
                });

                menuLinks.setPadding(false);
                menuLinks.setSpacing(true);
                menuLinks.setWidthFull();

                addMenuIfAllowed(menuLinks, "", "Dashboard", DashboardView.class, securityService);
                addMenuIfAllowed(menuLinks, "category", "Category", CategoryView.class, securityService);
                addMenuIfAllowed(menuLinks, "vendor", "Vendor", VendorView.class, securityService);
                addMenuIfAllowed(menuLinks, "unit", "Unit", UnitView.class, securityService);
                addMenuIfAllowed(menuLinks, "item", "Item", ItemView.class, securityService);
                addMenuIfAllowed(menuLinks, "item-variant", "Item Variant", ItemVariantView.class, securityService);
                addMenuIfAllowed(menuLinks, "purchase-request", "Purchase Request", PurchaseRequestView.class, securityService);
                addMenuIfAllowed(menuLinks, "request-for-quotation", "Request For Quotation", RequestForQuotationView.class, securityService);
                addMenuIfAllowed(menuLinks, "quotations", "Quotation", QuotationView.class, securityService);
                addMenuIfAllowed(menuLinks, "quotation-comparison", "Quotation Comparison", QuotationComparisonView.class, securityService);
                addMenuIfAllowed(menuLinks, "purchase-order", "Purchase Order", PurchaseOrderView.class, securityService);
                addMenuIfAllowed(menuLinks, "department", "Department", DepartmentView.class, securityService);
                addMenuIfAllowed(menuLinks, "department-budget", "Department Budget", DepartmentBudgetView.class, securityService);
                addMenuIfAllowed(menuLinks, "employee", "Employee", EmployeeView.class, securityService);
                addMenuIfAllowed(menuLinks, "role", "Role", RoleView.class, securityService);
                addMenuIfAllowed(menuLinks, "user", "User", UsersView.class, securityService);
                addMenuIfAllowed(menuLinks, "assigning-config", "Assigning Config", AssigningConfigView.class, securityService);
                addMenuIfAllowed(menuLinks, "repeated-periods", "Repeated Period", RepeatedPeriodView.class, securityService);
                addMenuIfAllowed(menuLinks, "audit-logs", "Audit Logs", AuditLogView.class, securityService);
                addMenuIfAllowed(menuLinks, "permission", "Permission", ViewPermissionView.class, securityService);

                Scroller scroller = new Scroller(menuLinks);
                scroller.setSizeFull();

                Button logoutButton = new Button("Logout", VaadinIcon.SIGN_OUT.create());
                logoutButton.setWidthFull();
                logoutButton.getStyle().set("margin-top", "10px");

                logoutButton.addClickListener(event -> {
                        SecurityContextHolder.clearContext();
                        getUI().ifPresent(ui -> {
                                ui.getSession().close();
                                ui.getPage().setLocation("/login");
                        });
                });

                VerticalLayout drawerLayout = new VerticalLayout(profileSection, scroller, logoutButton);
                drawerLayout.setSizeFull();
                drawerLayout.setPadding(false);
                drawerLayout.setSpacing(false);
                drawerLayout.expand(scroller);
                drawerLayout.getStyle().set("overflow", "hidden");

                addToDrawer(drawerLayout);
                getStyle().set("--vaadin-app-layout-drawer-width", "260px");
        }

        private void addMenuIfAllowed(
                        VerticalLayout layout,
                        String viewName,
                        String text,
                        Class<? extends Component> navigationTarget,
                        SecurityService securityService) {

                if (securityService.canAccessView(viewName)) {
                        layout.add(createLink(text, navigationTarget));
                }
        }

        private RouterLink createLink(
                        String text,
                        Class<? extends Component> navigationTarget) {

                RouterLink link = new RouterLink(text, navigationTarget);
                link.getStyle()
                                .set("padding", "10px 15px")
                                .set("border-radius", "8px")
                                .set("text-decoration", "none")
                                .set("font-size", "15px")
                                .set("display", "block")
                                .set("color", "var(--lumo-body-text-color)");

                return link;
        }

       @Override
        public void afterNavigation(AfterNavigationEvent event) {
                String currentRoutePath = event.getLocation().getPath();

                menuLinks.getChildren().forEach(component -> {
                        if (component instanceof RouterLink link) {
                                boolean matchesRoute = false;

                                
                                if (link.getHref().equals(currentRoutePath) || 
                                   (!link.getHref().isEmpty() && currentRoutePath.startsWith(link.getHref() + "/"))) {
                                        matchesRoute = true;
                                } else if (link.getHref().isEmpty() && currentRoutePath.isEmpty()) {
                                        matchesRoute = true; 
                                }

                                if (matchesRoute) {
                                        link.getStyle()
                                                .set("background-color", "var(--lumo-primary-color-10pct)")
                                                .set("color", "var(--lumo-primary-text-color)")
                                                .set("font-weight", "bold");
                                } else {
                                        link.getStyle()
                                                .set("background-color", "transparent")
                                                .set("color", "var(--lumo-body-text-color)")
                                                .set("font-weight", "normal");
                                }
                        }
                });
        }
}