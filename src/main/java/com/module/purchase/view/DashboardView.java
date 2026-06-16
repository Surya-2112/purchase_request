package com.module.purchase.view;

import java.time.Year;
import java.util.List;

import org.springframework.data.domain.PageRequest;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Users;
import com.module.purchase.entity.Vendor;
import com.module.purchase.entityDTO.PurchaseRequestDTO;
import com.module.purchase.entityDTO.QuotationDTO;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.Status;
import com.module.purchase.enums.ViewName;
import com.module.purchase.service.DepartmentBudgetService;
import com.module.purchase.service.PurchaseOrderHeaderService;
import com.module.purchase.service.PurchaseRequestHeaderService;
import com.module.purchase.service.QuotationService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PermitAll
public class DashboardView extends VerticalLayout {

    private final PurchaseRequestHeaderService purchaseRequestService;
    private final PurchaseOrderHeaderService purchaseOrderService;
    private final DepartmentBudgetService departmentBudgetService;
    private final QuotationService quotationService;
    private final SecurityService securityService;

    public DashboardView(
            PurchaseRequestHeaderService purchaseRequestService,
            PurchaseOrderHeaderService purchaseOrderService,
            DepartmentBudgetService departmentBudgetService,
            QuotationService quotationService,
            SecurityService securityService) {

        this.purchaseRequestService = purchaseRequestService;
        this.purchaseOrderService = purchaseOrderService;
        this.departmentBudgetService = departmentBudgetService;
        this.quotationService = quotationService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Dashboard");
        add(title);

        try {
            Users currentUser = securityService.getLoggedInUser();
            
            if (currentUser != null) {
                Employee currentEmployee = currentUser.getEmployee();

                if (currentEmployee == null) {
                    Vendor currentVendor = currentUser.getVendor();
                    add(
                        createVendorSummaryCards(currentVendor),
                        createVendorRecentQuotationsGrid(currentVendor)
                    );
                } 
                else {
                    if (currentEmployee.getRole() != null) {
                        List<EmployeeGroup> roleName = currentEmployee.getRole().getEmployeeGroups();
                        
                        boolean isManagement = false;

                        if(roleName.contains(EmployeeGroup.SUPER_ADMIN) ||roleName.contains(EmployeeGroup.MANAGER) || roleName.contains(EmployeeGroup.DIRECTOR) )
                        {
                            isManagement = true;
                        }

                        if (isManagement) {
                            add(createManagementSummaryCards());
                            
                            if (securityService.canAccessView("department-budget")) { 
                                add(createDepartmentWiseSpending());
                            }
                            
                            add(createGlobalRecentPurchaseRequests());
                        } 
                        else {
                            add(createEmployeeSelfSummaryCards(currentEmployee));
                            add(createEmployeeRecentPurchaseRequests(currentEmployee));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Component createManagementSummaryCards() {
        long totalPR = purchaseRequestService.countAll();
        long pendingPR = purchaseRequestService.countByStatus(Status.WAITING_APPROVAL);
        long totalPO = purchaseOrderService.countAll();
        long approvedPO = purchaseOrderService.countByStatus(Status.APPROVED);

        HorizontalLayout row = new HorizontalLayout(
                createCard("Total PR (All)", String.valueOf(totalPR), VaadinIcon.CLIPBOARD_TEXT.create(), ViewName.PURCHASE_REQUEST.getRoute()),
                createCard("Global Pending PR", String.valueOf(pendingPR), VaadinIcon.CLOCK.create(), ViewName.PURCHASE_REQUEST.getRoute()),
                createCard("Total PO (All)", String.valueOf(totalPO), VaadinIcon.CART.create(), "purchase-order"),
                createCard("Global Approved PO", String.valueOf(approvedPO), VaadinIcon.CHECK.create(), "purchase-order")
        );
        row.setWidthFull();
        return new VerticalLayout(row);
    }

    private Component createGlobalRecentPurchaseRequests() {
        VerticalLayout layout = new VerticalLayout();
        H3 title = new H3("Recent Global Purchase Requests");
        Grid<PurchaseRequestDTO> grid = new Grid<>();

        grid.addColumn(PurchaseRequestDTO::getPurchaseRequestId).setHeader("PR No");
        grid.addColumn(pr -> pr.getCreatedBy().getEmployeeName()).setHeader("Created By");
        grid.addColumn(pr -> pr.getForDepartment().getDepartmentName()).setHeader("Department");
        grid.addColumn(PurchaseRequestDTO::getStatus).setHeader("Status");

        grid.setItems(purchaseRequestService.getRecentPurchaseRequests(PageRequest.of(0, 3)));
        grid.addItemDoubleClickListener(event -> {
            PurchaseRequestDTO pr = event.getItem();
            getUI().ifPresent(ui -> ui.navigate(ViewName.PURCHASE_REQUEST_DETAILS.getRoute() + "/" + pr.getPurchaseRequestId()));
        });

        layout.add(title, grid);
        grid.setAllRowsVisible(true);
        return layout;
    }


    private Component createEmployeeSelfSummaryCards(Employee employee) {
        long myTotalPR = purchaseRequestService.countAllByEmployee(employee);
        long myPendingPR = purchaseRequestService.countByStatusAndEmployee(Status.WAITING_APPROVAL, employee);

        HorizontalLayout row = new HorizontalLayout(
                createCard("My Total PR", String.valueOf(myTotalPR), VaadinIcon.USER.create(), ViewName.PURCHASE_REQUEST.getRoute()),
                createCard("My Pending PR", String.valueOf(myPendingPR), VaadinIcon.CLOCK.create(), ViewName.PURCHASE_REQUEST.getRoute())
        );
        row.setWidthFull();
        return new VerticalLayout(row);
    }

    private Component createEmployeeRecentPurchaseRequests(Employee employee) {
        VerticalLayout layout = new VerticalLayout();
        H3 title = new H3("My Recent Requests");
        Grid<PurchaseRequestDTO> grid = new Grid<>();

        grid.addColumn(PurchaseRequestDTO::getPurchaseRequestId).setHeader("PR No");
        grid.addColumn(pr -> pr.getForDepartment().getDepartmentName()).setHeader("Department");
        grid.addColumn(PurchaseRequestDTO::getStatus).setHeader("Status");

        grid.setItems(purchaseRequestService.getRecentPurchaseRequestsByEmployee(employee, PageRequest.of(0, 3)));
        
        grid.addItemDoubleClickListener(event -> {
            PurchaseRequestDTO pr = event.getItem();
            getUI().ifPresent(ui -> ui.navigate(ViewName.PURCHASE_REQUEST_DETAILS.getRoute() + "/" + pr.getPurchaseRequestId()));
        });

        layout.add(title, grid);
        grid.setAllRowsVisible(true);
        return layout;
    }
    
    
    private Component createVendorSummaryCards(Vendor vendor) {
        long requestForQuotations = quotationService.countRFQForVendor(vendor);
        long pendingQuotations = quotationService.countByStatusForVendor(Status.WAITING_APPROVAL, vendor);
        long approvedQuotations = quotationService.countByStatusForVendor(Status.APPROVED, vendor);

        HorizontalLayout row = new HorizontalLayout(
                createCard("Request for Quotations", String.valueOf(requestForQuotations), VaadinIcon.QUESTION_CIRCLE.create(), "rfq-view"),
                createCard("Pending Quotations", String.valueOf(pendingQuotations), VaadinIcon.CLOCK.create(), "quotation-view"),
                createCard("Approved Quotations", String.valueOf(approvedQuotations), VaadinIcon.CHECK.create(), "quotation-view")
        );
        row.setWidthFull();
        return new VerticalLayout(row);
    }

    private Component createVendorRecentQuotationsGrid(Vendor vendor) {
        VerticalLayout layout = new VerticalLayout();
        H3 title = new H3("Your Recent Quotations");
        Grid<QuotationDTO> grid = new Grid<>();

        grid.addColumn(QuotationDTO::getId).setHeader("Quotation No");
        grid.addColumn(q -> "₹ " + q.getTotalAmount()).setHeader("Total Price");
        grid.addColumn(QuotationDTO::getStatus).setHeader("Status");

        grid.setItems(quotationService.getRecentQuotationsForVendor(vendor, PageRequest.of(0, 3)));
        
        grid.addItemDoubleClickListener(event -> {
            QuotationDTO quotation = event.getItem();
            getUI().ifPresent(ui -> ui.navigate("quotation-details/" + quotation.getId()));
        });

        layout.add(title, grid);
        grid.setAllRowsVisible(true);
        return layout;
    }

    // ==========================================
    // COMMON HELPERS
    // ==========================================

    private Div createCard(String title, String value, Icon icon, String navigationUrl) {
        H3 valueText = new H3(value);
        Span titleText = new Span(title);
        HorizontalLayout header = new HorizontalLayout(icon, titleText);
        VerticalLayout content = new VerticalLayout(header, valueText);

        content.setPadding(false);
        content.setSpacing(false);

        Div card = new Div(content);
        card.getStyle()
                .set("padding", "20px")
                .set("border-radius", "14px")
                .set("background", "white")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("cursor", "pointer")
                .set("min-width", "200px");

        card.addClickListener(event -> UI.getCurrent().navigate(navigationUrl));
        return card;
    }
    
    private Component createDepartmentWiseSpending() {
        VerticalLayout layout = new VerticalLayout();
        H3 title = new H3("Department Wise Spending");
        Grid<DepartmentBudget> grid = new Grid<>();

        grid.addColumn(db -> db.getDepartment().getDepartmentName()).setHeader("Department");
        grid.addColumn(db -> "₹ " + db.getTotalBudgetAmount()).setHeader("Total Budget");
        grid.addColumn(db -> "₹ " + db.getRemainingBudgetAmount()).setHeader("Remaining Amount");
        grid.addColumn(db -> "₹ " + (db.getTotalBudgetAmount() - db.getRemainingBudgetAmount())).setHeader("Spent Amount");

        grid.setItems(departmentBudgetService.getDepartmentSpendingData(Year.now()));
        grid.addItemDoubleClickListener(event -> {
            DepartmentBudget db = event.getItem();
            getUI().ifPresent(ui -> ui.navigate(ViewName.DEPARTMENT_BUDGET_DETAILS.getRoute() + "/" + db.getDepartmentBudgetId()));
        });

        layout.add(title, grid);
        grid.setAllRowsVisible(true);
        return layout;
    }
}