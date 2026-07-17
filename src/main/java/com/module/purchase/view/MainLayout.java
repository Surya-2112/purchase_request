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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon; 
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
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

                String employeeName = loggedInUser.getEmployee() == null ? loggedInUser.getUserName()
                                : loggedInUser.getEmployee().getEmployeeName();

                H2 title = new H2("Purchase Management");

                title.getStyle().set("margin", "0")
                                .setColor("rgb(248, 250, 252)")
                                .setFontWeight(900)
                                .set("font-size", "2.2rem")
                                .setAlignItems(Style.AlignItems.CENTER);

                HorizontalLayout header = new HorizontalLayout(title);

                header.setWidthFull();
                header.setPadding(true);
                header.setAlignItems(FlexComponent.Alignment.CENTER);
                header.getStyle().set("background-color", "rgb(33, 43, 54)").set("border","1px solid rgb(255, 255, 255)");

                addToNavbar(header);

                Avatar avatar = new Avatar(employeeName);
                avatar.setWidth("45px");
                avatar.setHeight("45px");
                avatar.getStyle().set("background-color", "rgb(188, 253, 237)").set("color", "rgb(33, 43, 54)");
                avatar.setColorIndex(0);

                H3 userName = new H3(employeeName);
                userName.getStyle().set("margin", "0")
                                .set("font-size", "18px")
                                .set("color", "rgb(248, 250, 252)");

                VerticalLayout profileText = new VerticalLayout(userName);
                profileText.setPadding(false);
                profileText.setSpacing(false);

                HorizontalLayout profileSection = new HorizontalLayout(avatar, profileText);

                profileSection.setAlignItems(FlexComponent.Alignment.CENTER);
                profileSection.setWidthFull();
                profileSection.setPadding(true);
                profileSection.getStyle().set("cursor", "pointer")
                                        .set("border-bottom","1px solid rgb(255, 255, 255)")
                                        .set("background-color", "rgb(33, 43, 54)");

                profileSection.addClickListener(event -> {
                        getUI().ifPresent(ui -> ui.navigate(ViewName.USER_DETAILS.getRoute() + "/" + loggedInUser.getUserId()));
                });

                menuLinks.setPadding(false);
                menuLinks.setSpacing(true);
                menuLinks.setWidthFull();

                addMenuIfAllowed(menuLinks, "", "Dashboard", VaadinIcon.DASHBOARD, DashboardView.class, securityService);
                addMenuIfAllowed(menuLinks, "category", "Category", VaadinIcon.TAGS, CategoryView.class,securityService);
                addMenuIfAllowed(menuLinks, "vendor", "Vendor", VaadinIcon.TRUCK, VendorView.class, securityService);
                addMenuIfAllowed(menuLinks, "unit", "Unit", VaadinIcon.SCALE_UNBALANCE, UnitView.class, securityService);
                addMenuIfAllowed(menuLinks, "item", "Item", VaadinIcon.PACKAGE, ItemView.class, securityService);
                addMenuIfAllowed(menuLinks, "item-variant", "Item Variant", VaadinIcon.CUBES, ItemVariantView.class, securityService);
                addMenuIfAllowed(menuLinks, "purchase-request", "Purchase Request", VaadinIcon.CLIPBOARD, PurchaseRequestView.class, securityService);
                addMenuIfAllowed(menuLinks, "request-for-quotation", "Request For Quotation", VaadinIcon.QUESTION_CIRCLE_O, RequestForQuotationView.class, securityService);
                addMenuIfAllowed(menuLinks, "quotations", "Quotation", VaadinIcon.INVOICE, QuotationView.class, securityService);
                addMenuIfAllowed(menuLinks, "quotation-comparison", "Quotation Comparison", VaadinIcon.EXCHANGE,QuotationComparisonView.class, securityService);
                addMenuIfAllowed(menuLinks, "purchase-order", "Purchase Order", VaadinIcon.CART, PurchaseOrderView.class, securityService);
                addMenuIfAllowed(menuLinks, "department", "Department", VaadinIcon.OFFICE, DepartmentView.class, securityService);
                addMenuIfAllowed(menuLinks, "department-budget", "Department Budget", VaadinIcon.MONEY,DepartmentBudgetView.class, securityService);
                addMenuIfAllowed(menuLinks, "employee", "Employee", VaadinIcon.USER_CARD, EmployeeView.class, securityService);
                addMenuIfAllowed(menuLinks, "role", "Role", VaadinIcon.KEY, RoleView.class, securityService);
                addMenuIfAllowed(menuLinks, "user", "User", VaadinIcon.USER, UsersView.class, securityService);
                addMenuIfAllowed(menuLinks, "assigning-config", "Assigning Config", VaadinIcon.COGS,AssigningConfigView.class, securityService);
                addMenuIfAllowed(menuLinks, "repeated-periods", "Repeated Period", VaadinIcon.CALENDAR_CLOCK, RepeatedPeriodView.class, securityService);
                addMenuIfAllowed(menuLinks, "audit-logs", "Audit Logs", VaadinIcon.RECORDS, AuditLogView.class, securityService);
                addMenuIfAllowed(menuLinks, "permission", "Permission", VaadinIcon.SHIELD, ViewPermissionView.class, securityService);
                Scroller scroller = new Scroller(menuLinks);
                scroller.getStyle().set("border-bottom","1px solid rgb(255,255,255)");
                scroller.setSizeFull();

                Button logoutButton = new Button("Logout", VaadinIcon.SIGN_OUT.create());
                logoutButton.setWidthFull();
                logoutButton.getStyle().set("margin-top", "10px").setFontWeight("800").setColor("rgb(255,255,255)");

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
                drawerLayout.getStyle().set("overflow", "hidden")
                                .set("border","2px solid rgb(255, 255, 255)")
                                .set("background-color", "rgb(33, 43, 54)");

                addToDrawer(drawerLayout);
                getStyle().set("--vaadin-app-layout-drawer-width", "18%")
                                .set("background-color", "rgb(255, 255, 200)");
        }

        private void addMenuIfAllowed(VerticalLayout layout, String viewName, String text, VaadinIcon icon,
                        Class<? extends Component> navigationTarget, SecurityService securityService) {

                if (securityService.canAccessView(viewName)) {
                        layout.add(createLink(text, icon, navigationTarget));
                }
        }

        private RouterLink createLink(String text,VaadinIcon icon,Class<? extends Component> navigationTarget) {

                RouterLink link = new RouterLink(navigationTarget);

                Icon iconComponent = icon.create();
                iconComponent.getStyle()
                                .set("margin-right", "12px")
                                .set("width", "20px")
                                .set("height", "20px");

                link.add(iconComponent, new Span(text));

                link.getStyle().set("padding", "10px 15px")
                                .set("text-decoration", "none")
                                .set("font-size", "15px")
                                .set("display", "block")
                                 .set("color", "rgb(250, 250, 250)");

                return link;
        }

        @Override
        public void afterNavigation(AfterNavigationEvent event) {
                String currentRoutePath = event.getLocation().getPath();

                if (currentRoutePath.contains("detail") || currentRoutePath.contains("edit")
                                || currentRoutePath.contains("form")) {
                        return;
                }

                menuLinks.getChildren().forEach(component -> {
                        if (component instanceof RouterLink link) {
                                boolean matchesRoute = false;

                                if (link.getHref().equals(currentRoutePath) ||
                                                (!link.getHref().isEmpty()
                                                                && currentRoutePath.startsWith(link.getHref() + "/"))) {
                                        matchesRoute = true;
                                } else if (link.getHref().isEmpty() && currentRoutePath.isEmpty()) {
                                        matchesRoute = true;
                                }

                                if (matchesRoute) {
                                        link.getStyle().set("background-color", "rgb(250, 250, 250)")
                                                        .set("color", "var(--lumo-primary-text-color)")
                                                        .setWidth("87.1%")
                                                        .set("font-weight", "bold");
                                } else {
                                        link.getStyle().set("background-color", "transparent")
                                                        .set("color", "rgb(250, 250, 250)")
                                                        .set("font-weight", "normal");
                                }
                        }
                });
        }
}