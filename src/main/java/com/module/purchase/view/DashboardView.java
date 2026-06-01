package com.module.purchase.view;

import java.time.Year;

import org.springframework.data.domain.PageRequest;

import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entityDTO.PurchaseOrderDTO;
import com.module.purchase.entityDTO.PurchaseRequestDTO;
import com.module.purchase.enums.Status;
import com.module.purchase.service.PurchaseOrderHeaderService;
import com.module.purchase.service.DepartmentBudgetService;
import com.module.purchase.service.PurchaseRequestHeaderService;
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
    private final  DepartmentBudgetService departmentBudgetService;

    public DashboardView(
            PurchaseRequestHeaderService purchaseRequestService,
            PurchaseOrderHeaderService purchaseOrderService,
            DepartmentBudgetService departmentBudgetService) {

        this.purchaseRequestService = purchaseRequestService;
        this.purchaseOrderService = purchaseOrderService;
        this.departmentBudgetService=departmentBudgetService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Dashboard");

        add(
                title,
                createSummaryCards(),
                createDepartmentWiseSpending(),
                createRecentPurchaseRequests(),
                createRecentPurchaseOrders()
        );
    }

    private Component createSummaryCards() {

        long totalPR = purchaseRequestService.countAll();

        long pendingPR = purchaseRequestService.countByStatus(Status.WAITING_APPROVAL);

        long approvedPR = purchaseRequestService.countByStatus(Status.APPROVED);

        long rejectedPR = purchaseRequestService.countByStatus(Status.REJECTED);

        // long totalPO = purchaseOrderService.countAll();

        // long draftPO = purchaseOrderService.countByStatus(Status.DRAFT);

        // long approvedPO = purchaseOrderService.countByStatus(Status.APPROVED);

        // long rejectedPO = purchaseOrderService.countByStatus(Status.REJECTED);

        HorizontalLayout row1 = new HorizontalLayout(
                createCard(
                        "Total PR",
                        String.valueOf(totalPR),
                        VaadinIcon.CLIPBOARD_TEXT.create(),
                        "purchase-request"
                ),

                createCard(
                        "Pending Approval",
                        String.valueOf(pendingPR),
                        VaadinIcon.CLOCK.create(),
                        "purchase-request"
                ),

                createCard(
                        "Approved",
                        String.valueOf(approvedPR),
                        VaadinIcon.CHECK.create(),
                        "purchase-request"
                ),

                createCard(
                        "Rejected",
                        String.valueOf(rejectedPR),
                        VaadinIcon.CLOSE.create(),
                        "purchase-request"
                )
        );

        // HorizontalLayout row2 = new HorizontalLayout(
        //         createCard(
        //                 "Total PO",
        //                 String.valueOf(totalPO),
        //                 VaadinIcon.CART.create(),
        //                 "purchase-order"
        //         ),

        //         createCard(
        //                 "Draft PO",
        //                 String.valueOf(draftPO),
        //                 VaadinIcon.FILE_TEXT.create(),
        //                 "purchase-order"
        //         ),

        //         createCard(
        //                 "Approved",
        //                 String.valueOf(approvedPO),
        //                 VaadinIcon.CHECK.create(),
        //                 "purchase-order"
        //         ),

        //         createCard(
        //                 "Rejected",
        //                 String.valueOf(rejectedPO),
        //                 VaadinIcon.CLOSE.create(),
        //                 "purchase-order"
        //         )
        // );

        row1.setWidthFull();
        // row2.setWidthFull();

        VerticalLayout layout = new VerticalLayout(row1
                // ,row2
                );

        return layout;
    }

    private Div createCard(
            String title,
            String value,
            Icon icon,
            String navigationUrl) {

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

        card.addClickListener(event -> {
            UI.getCurrent().navigate(navigationUrl);
        });

        return card;
    }

    // =========================================================
    // DEPARTMENT SPENDING
    // =========================================================

    private Component createDepartmentWiseSpending() {

        VerticalLayout layout = new VerticalLayout();

        H3 title = new H3("Department Wise Spending");

        Grid<DepartmentBudget> grid = new Grid<>();

        grid.addColumn(db -> db.getDepartment().getDepartmentName())
                .setHeader("Department");

        grid.addColumn(db -> "₹ " + db.getTotalBudgetAmount())
                .setHeader("Total Budget");

        grid.addColumn(db -> "₹"+ db.getRemainingBudgetAmount())
                .setHeader("Remaining Amount");
        grid.addColumn(db -> "₹" + (db.getTotalBudgetAmount()-db.getRemainingBudgetAmount()))
                .setHeader("Spended Amount");

        grid.setItems(departmentBudgetService.getDepartmentSpendingData(Year.now()));

        grid.addItemDoubleClickListener(event -> {

              DepartmentBudget departmentBudget = event.getItem();

                getUI().ifPresent(ui -> ui.navigate("department-budget-details/"
                                                + departmentBudget.getDepartmentBudgetId()));
        });


        layout.add(title, grid);

        grid.setAllRowsVisible(true);

        return layout;
    }


    private Component createRecentPurchaseRequests() {

        VerticalLayout layout = new VerticalLayout();

        H3 title = new H3("Recent Purchase Requests");

        Grid<PurchaseRequestDTO> grid = new Grid<>();

        grid.addColumn(PurchaseRequestDTO::getPurchaseRequestId)
                .setHeader("PR No");

        grid.addColumn(pr -> pr.getCreatedBy().getEmployeeName())
                .setHeader("CreatedBy");

        grid.addColumn(pr -> pr.getForDepartment().getDepartmentName())
                .setHeader("Department");

        grid.addColumn(PurchaseRequestDTO::getStatus)
                .setHeader("Stauts");
        

        grid.setItems(
                purchaseRequestService
                        .getRecentPurchaseRequests(
                                PageRequest.of(0, 3)
                        )
        );

        grid.addItemDoubleClickListener(event -> {
                PurchaseRequestDTO pr = event.getItem();
              getUI().ifPresent(ui ->ui.navigate( "purchase-request-details/" + pr.getPurchaseRequestId()));
        });

        layout.add(title, grid);
         grid.setAllRowsVisible(true);

        return layout;
    }

    // =========================================================
    // RECENT PURCHASE ORDERS
    // =========================================================

    private Component createRecentPurchaseOrders() {

        VerticalLayout layout = new VerticalLayout();

        H3 title = new H3("Recent Purchase Orders");

        Grid<PurchaseOrderDTO> grid = new Grid<>();

        grid.addColumn(PurchaseOrderDTO::getPurchaseOrderId)
                .setHeader("PO No");

        grid.addColumn(po -> (po.getVendor()!=null) ? po.getVendor().getVendorName(): "-")
                .setHeader("Vendor");

        grid.addColumn(po -> po.getPurchaseRequestHeader().getPurchaseRequestId())
                .setHeader("PR No");
        
        grid.addColumn(PurchaseOrderDTO::getStatus)
                .setHeader("Stauts");

        grid.setItems(
                purchaseOrderService
                        .getRecentPurchaseOrders(
                                PageRequest.of(0, 3)
                        )
        );

        grid.addItemClickListener(event -> {

           PurchaseOrderDTO po=event.getItem();
               getUI().ifPresent(ui ->
                        ui.navigate("purchase-order-details/"+ po.getPurchaseOrderId()));
        });
        layout.add(title, grid);
         grid.setAllRowsVisible(true);
        return layout;
    }
}