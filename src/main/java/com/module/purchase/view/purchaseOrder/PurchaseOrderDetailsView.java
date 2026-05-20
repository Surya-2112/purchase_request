package com.module.purchase.view.purchaseOrder;

import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.PurchaseOrderLine;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.PurchaseOrderHeaderService;
import com.module.purchase.service.PurchaseOrderLineService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-order-details/:id", layout = MainLayout.class)
@PermitAll
public class PurchaseOrderDetailsView
        extends VerticalLayout
        implements BeforeEnterObserver {

    private final PurchaseOrderHeaderService headerService;

    private final PurchaseOrderLineService lineService;

    private final AssigningApprovalsService approvalsService;

    private PurchaseOrderHeader header;

    // =========================================================
    // HEADER UI
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

    private final Span status =
            new Span();

    // =========================================================
    // GRIDS
    // =========================================================

    private final Grid<PurchaseOrderLine> lineGrid =
            new Grid<>(PurchaseOrderLine.class, false);

    private final Grid<AssigningApprovals> approvalGrid =
            new Grid<>(AssigningApprovals.class, false);

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public PurchaseOrderDetailsView(

            PurchaseOrderHeaderService headerService,

            PurchaseOrderLineService lineService,

            AssigningApprovalsService approvalsService) {

        this.headerService = headerService;

        this.lineService = lineService;

        this.approvalsService = approvalsService;

        setSizeFull();

        setPadding(false);

        setSpacing(false);

        configureGrids();

        // =====================================================
        // HEADER SECTION
        // =====================================================

        VerticalLayout headerSection =
                buildHeaderSection();

        headerSection.setWidthFull();

        // =====================================================
        // MAIN CONTENT
        // =====================================================

        VerticalLayout content =
                new VerticalLayout(

                        new H2("Purchase Order Details"),

                        headerSection,

                        new H3("Purchase Order Lines"),

                        lineGrid,

                        new H3("Approval Flow"),

                        approvalGrid
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
    // ROUTE LOAD
    // =========================================================

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        Long id =
                Long.parseLong(

                        event.getRouteParameters()
                                .get("id")
                                .get()
                );

        header =
                headerService
                        .getPurchaseOrderHeaderById(id)
                        .orElseThrow(() ->

                                new RuntimeException(
                                        "Purchase Order not found"
                                )
                        );

        bindHeader();

        loadGrids();
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

                        status
                );

        layout.setPadding(true);

        layout.setSpacing(false);

        layout.setWidthFull();

        layout.getStyle()

                .set("background", "#f9f9f9")

                .set("border", "1px solid #ddd")

                .set("border-radius", "8px");

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

                                ? header.getPurchaseRequestHeader().getForDepartment().getDepartmentName()
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

        status.setText(
                "Status : "
                        + header.getStatus()
        );
    }

    // =========================================================
    // CONFIGURE GRIDS
    // =========================================================

    private void configureGrids() {

        // =====================================================
        // LINE GRID
        // =====================================================

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

        lineGrid.setWidthFull();

        lineGrid.setAllRowsVisible(true);

        // =====================================================
        // APPROVAL GRID
        // =====================================================

        approvalGrid.addColumn(
                AssigningApprovals::getLevel
        ).setHeader("Level");

        approvalGrid.addColumn(a ->

                a.getApprover() != null

                        ? a.getApprover()
                        .getEmployeeName()

                        : ""

        ).setHeader("Approver");

        approvalGrid.addColumn(
                AssigningApprovals::getAssignedDate
        ).setHeader("Assigned Date");

        approvalGrid.addColumn(a ->

                a.getStatus() != null

                        ? a.getStatus().name()

                        : ""

        ).setHeader("Status");

        approvalGrid.setWidthFull();

        approvalGrid.setAllRowsVisible(true);
    }

    // =========================================================
    // LOAD DATA
    // =========================================================

    private void loadGrids() {

        lineGrid.setItems(

                lineService
                        .getPurchaseOrderLineByHeader(
                                header
                        )
        );

        approvalGrid.setItems(

                approvalsService
                        .getAssigningApprovalByTypeAndReferId(

                                ApprovalType.PURCHASE_ORDER_APPROVAL,

                                header.getPurchaseOrderId()
                        )
        );
    }
}