package com.module.purchase.view.purchaseOrder;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.PurchaseOrderLine;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.DepartmentBudgetService;
import com.module.purchase.service.PurchaseOrderHeaderService;
import com.module.purchase.service.PurchaseOrderLineService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "assigning-approvals-orders-details/:id", layout = MainLayout.class)
@PermitAll
public class AssigningApprovalsOrdersDetailsView
        extends VerticalLayout
        implements BeforeEnterObserver {

    private final SecurityService securityService;

    private final PurchaseOrderHeaderService headerService;

    private final PurchaseOrderLineService lineService;

    private final AssigningApprovalsService approvalsService;

    private final DepartmentBudgetService departmentBudgetService;

    private PurchaseOrderHeader header;

    private AssigningApprovals approval;

    // =========================================================
    // HEADER
    // =========================================================

    private final Span orderId =
            new Span();

    private final Span requestId =
            new Span();

    private final Span createdBy =
            new Span();

    private final Span department =
            new Span();

    private final Span vendor =
            new Span();

    private final Span totalAmount =
            new Span();

    private final Span createdDate =
            new Span();

    private final Span level =
            new Span();

    private final Span status =
            new Span();

    // =========================================================
    // BUDGET
    // =========================================================

    private final H3 budgetTitle =
            new H3();

    private final Span budgetYear =
            new Span();

    private final Span totalBudgetAmount =
            new Span();

    private final Span remainingBudgetAmount =
            new Span();

    // =========================================================
    // COMMENTS
    // =========================================================

    private final TextArea comments =
            new TextArea("Comments");

    // =========================================================
    // GRID
    // =========================================================

    private final Grid<PurchaseOrderLine> lineGrid =
            new Grid<>(PurchaseOrderLine.class, false);

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AssigningApprovalsOrdersDetailsView( PurchaseOrderHeaderService headerService,
            PurchaseOrderLineService lineService,AssigningApprovalsService approvalsService,
            DepartmentBudgetService departmentBudgetService,SecurityService securityService) {

        this.headerService = headerService;

        this.lineService = lineService;

        this.approvalsService = approvalsService;

        this.departmentBudgetService = departmentBudgetService;
        this.securityService =securityService;

        setSizeFull();

        setPadding(false);

        setSpacing(false);

        comments.setWidthFull();

        comments.setMinHeight("120px");

        configureLineGrid();

        Button approveBtn =
                new Button("Approve");

        Button rejectBtn =
                new Button("Reject");

        approveBtn.addClickListener(e ->
                approveOrder());

        rejectBtn.addClickListener(e ->
                rejectOrder());

        HorizontalLayout buttonLayout =
                new HorizontalLayout(

                        approveBtn,

                        rejectBtn
                );

        VerticalLayout content =
                new VerticalLayout(

                        new H2("Purchase Order Approval Details"),

                        buildHeaderSection(),

                        buildBudgetSection(),

                        new H3("Purchase Order Lines"),

                        lineGrid,

                        comments,

                        buttonLayout
                );

        content.setWidthFull();

        content.setPadding(true);

        content.setSpacing(true);

        Scroller scroller =
                new Scroller(content);

        scroller.setSizeFull();

        add(scroller);
    }

    // =========================================================
    // LOAD
    // =========================================================

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        Long approvalId =
                Long.parseLong(

                        event.getRouteParameters()
                                .get("id")
                                .get()
                );

        approval =
                approvalsService
                        .getAssigningApprovalById(
                                approvalId
                        )
                        .orElseThrow(() ->

                                new RuntimeException(
                                        "Approval not found"
                                )
                        );

        header =
                headerService
                        .getPurchaseOrderHeaderById(
                                approval.getReferenceId()
                        )
                        .orElseThrow(() ->

                                new RuntimeException(
                                        "Purchase Order not found"
                                )
                        );

        bindHeader();

        loadDepartmentBudget();

        loadLines();
    }

    // =========================================================
    // HEADER SECTION
    // =========================================================

    private VerticalLayout buildHeaderSection() {

        VerticalLayout layout =
                new VerticalLayout(

                        orderId,

                        requestId,

                        createdBy,

                        department,

                        vendor,

                        totalAmount,

                        createdDate,

                        level,

                        status
                );

        layout.setSpacing(false);

        layout.setPadding(false);

        return layout;
    }

    // =========================================================
    // BUDGET SECTION
    // =========================================================

    private VerticalLayout buildBudgetSection() {

        VerticalLayout layout =
                new VerticalLayout(

                        budgetTitle,

                        budgetYear,

                        totalBudgetAmount,

                        remainingBudgetAmount
                );

        layout.setSpacing(false);

        layout.setPadding(false);

        return layout;
    }

    // =========================================================
    // BIND HEADER
    // =========================================================

    private void bindHeader() {

        orderId.setText(
                "Purchase Order ID : "
                        + header.getPurchaseOrderId()
        );

        requestId.setText(
                "Purchase Request ID : "
                        + (

                        header.getPurchaseRequestHeader() != null

                                ? header.getPurchaseRequestHeader()
                                .getPurchaseRequestId()

                                : "-"
                )
        );

        createdBy.setText(
                "Created By : "
                        + (

                        header.getCreatedBy() != null

                                ? header.getCreatedBy()
                                .getEmployeeName()

                                : "-"
                )
        );

        department.setText(
                "Department : "
                        + (

                        header.getPurchaseRequestHeader().getForDepartment() != null

                                ? header.getPurchaseRequestHeader().getForDepartment()
                                .getDepartmentName()

                                : "-"
                )
        );

        vendor.setText(
                "Vendor : "
                        + (

                        header.getVendor() != null

                                ? header.getVendor()
                                .getVendorName()

                                : "-"
                )
        );

        totalAmount.setText(
                "Total Amount : "
                        + header.getTotalAmount()
        );

        createdDate.setText(
                "Created Date : "
                        + header.getCreatedDate()
        );

        level.setText(
                "Approval Level : "
                        + approval.getLevel()
        );

        status.setText(
                "Approval Status : "
                        + approval.getStatus()
        );
    }

    // =========================================================
    // LOAD BUDGET
    // =========================================================

    private void loadDepartmentBudget() {

        if (header.getPurchaseRequestHeader().getForDepartment() == null) {
            return;
        }

        Department dept =
                header.getPurchaseRequestHeader().getForDepartment();

        DepartmentBudget budget =
                departmentBudgetService
                        .getByDepartmentAndYear(
                                dept,
                                Year.now()
                        );

        if (budget == null) {

            budgetTitle.setText(
                    "Department Budget Not Configured"
            );

            budgetYear.setText("");

            totalBudgetAmount.setText("");

            remainingBudgetAmount.setText("");

            return;
        }

        budgetTitle.setText(
                "Department Budget"
        );

        budgetYear.setText(
                "Year : "
                        + budget.getYear()
        );

        totalBudgetAmount.setText(
                "Total Budget Amount : "
                        + budget.getTotalBudgetAmount()
        );

        remainingBudgetAmount.setText(
                "Remaining Budget Amount : "
                        + budget.getRemainingBudgetAmount()
        );
    }

    // =========================================================
    // GRID
    // =========================================================

    private void configureLineGrid() {

        lineGrid.setWidthFull();

        lineGrid.addColumn(
                PurchaseOrderLine::getPurchaseOrderLineId
        ).setHeader("Line ID");

        lineGrid.addColumn(line ->

                line.getItem() != null

                        ? line.getItem()
                        .getItemName()

                        : ""

        ).setHeader("Item Name");

        lineGrid.addColumn(
                PurchaseOrderLine::getQuantity
        ).setHeader("Quantity");

        lineGrid.addColumn(
                PurchaseOrderLine::getUnitPrice
        ).setHeader("Unit Price");

        lineGrid.addColumn(
                PurchaseOrderLine::getDiscount
        ).setHeader("Discount");

        lineGrid.addColumn(
                PurchaseOrderLine::getTotalPrice
        ).setHeader("Total Price");
    }

    // =========================================================
    // LOAD LINES
    // =========================================================

    private void loadLines() {

        List<PurchaseOrderLine> lines =

                lineService
                        .getPurchaseOrderLineByHeader(
                                header
                        );

        lineGrid.setItems(lines);
    }

    // =========================================================
    // APPROVE
    // =========================================================

    private void approveOrder() {

        approval.setStatus(Status.APPROVED);

        approval.setComments(comments.getValue());

        approval.setApprovedDate(LocalDate.now());

        approvalsService.updateApprovals(approval,securityService.getLoggedInUser().getEmployee());

        Notification.show("Purchase Order Approved" );

        getUI().ifPresent(ui ->

                ui.navigate("purchase-order")
        );
    }

    // =========================================================
    // REJECT
    // =========================================================

    private void rejectOrder() {

        approval.setStatus( Status.REJECTED );

        approval.setComments(comments.getValue());

        approval.setApprovedDate(LocalDate.now());

        approvalsService.updateApprovals(approval,securityService.getLoggedInUser().getEmployee());

        Notification.show("Purchase Order Rejected");

        getUI().ifPresent(ui ->

                ui.navigate("purchase-order")
        );
    }
}